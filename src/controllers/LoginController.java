/*
 * Automatically translates to French based on user's computer settings
 */

package controllers;

import DBAccess.DBAppointments;
import DBAccess.DBContacts;
import DBAccess.DBCustomers;
import DBAccess.DBUsers;
import SchedulingApplication.FileIO;

import interfaces.GeneralInterfaces;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import Database.DBConnection;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import model.Appointments;
import model.ExceptionHandlers;
import model.SceneChange;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


public class LoginController implements Initializable {

    @FXML
    private Label usernameLbl;

    @FXML
    private Label passwordLbl;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginBtn;

    @FXML
    private Button exitBtn;

    @FXML
    private Label greetingLbl;

    @FXML
    private Label locationLbl;

    @FXML
    private Label timeZoneLbl;

    /**
     * Checks username and password and continues to main menu if correct or displays error if not.
     * @param event The login button is clicked
     */
    @FXML
    void onActionLogin(ActionEvent event) {
        String username = usernameTextField.getText();
        char[] password = passwordField.getText().toCharArray();
        passwordField.clear();
        boolean loginSuccess = DBUsers.checkUser(username, password);
        password = null;

        if (loginSuccess) {
            try {
                //log successful login in login.txt
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String s = " successfully logged in at ";
                FileIO.write(username + s + timestamp);

                SceneChange.sceneChange(event, "/view/MainMenu.fxml", "Main Menu");

                //display appointments within 15 minutes
                List<Appointments> a = DBAppointments.appointmentsToday(LocalDateTime.now());
                if (!a.isEmpty()) {
                    for (Appointments appointment : a) {
                        Alert upcomingAppointmentAlert = new Alert(Alert.AlertType.INFORMATION, "Upcoming Appointment");

                        upcomingAppointmentAlert.setContentText("There is an upcoming appointment in " +
                                Duration.between(LocalTime.now(),
                                        appointment.getStartTime()).toMinutes() + " minute(s) " +
                                "\n\nAppointment Time: " + appointment.getStartTime() +
                                "\nCustomer: " + DBCustomers.getCustomerById(appointment.getCustomerId()).getName() +
                                "\nContact: " + DBContacts.getContactById(appointment.getContactId()).getName() +
                                "\nLocation: " + appointment.getLocation());
                        upcomingAppointmentAlert.setTitle("Upcoming Appointments");
                        upcomingAppointmentAlert.showAndWait();
                    }
                } else {
                    Alert upcomingAppointmentAlert = new Alert(Alert.AlertType.INFORMATION, "There are no appointments in the next 15 minutes.", ButtonType.OK);
                    upcomingAppointmentAlert.setTitle("Upcoming Appointments");
                    upcomingAppointmentAlert.showAndWait();
                }
            } catch (NullPointerException | IOException e) {
                Alert fatalErrorAlert = new Alert(Alert.AlertType.ERROR, "Unable to reach main menu.  Application will now close.", ButtonType.OK);
                fatalErrorAlert.showAndWait();
                DBConnection.closeConnection();
                System.exit(-1);
            }
        }
        else //unsuccessful login
        {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            FileIO.write("Attempted login by " + username + " failed at " + timestamp);
            Alert loginErrorAlert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);

            try {
                ResourceBundle rb = ResourceBundle.getBundle("SchedulingApplication/Nat", Locale.getDefault());
                loginErrorAlert.setContentText(rb.getString("EMessage"));

            } catch (MissingResourceException e) {
                //stay in English
                loginErrorAlert.setContentText("Invalid username or password.");
            }

            loginErrorAlert.showAndWait();
        }
    }

    /**
     * Closes the program when the exit button is clicked.
     * @param event The exit button click
     */
    @FXML
    void onActionExit(ActionEvent event) throws Exception {
        DBConnection.closeConnection();
        System.exit(0);
    }

    /**
     * Initializes the login page in English or French and adds user's location and time zone based on system default locale.
     * @param url The location used
     * @param rb The resources used
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        try {
            rb = ResourceBundle.getBundle("SchedulingApplication/Nat", Locale.getDefault());
            greetingLbl.setText(rb.getString("Greeting"));
            greetingLbl.setTextAlignment(TextAlignment.CENTER);
            usernameLbl.setText(rb.getString("Username"));
            passwordLbl.setText(rb.getString("Password"));
            loginBtn.setText(rb.getString("Login"));
            exitBtn.setText(rb.getString("Exit"));

        } catch (MissingResourceException e) {
            //do nothing, stay in English
        }

        //display country on login
        GeneralInterfaces.Country country = userCountry -> String.valueOf(userCountry.getDisplayCountry());
        locationLbl.setText(country.getCountryString(Locale.getDefault()));

        //get TimeZone
        GeneralInterfaces.UserTimeZone getUserTimeZone = (Locale l) -> TimeZone.getDefault().getDisplayName(l);
        timeZoneLbl.setText(getUserTimeZone.userTime(Locale.getDefault()));
    }

}

