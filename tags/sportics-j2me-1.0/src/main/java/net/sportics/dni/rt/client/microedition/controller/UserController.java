/* Copyright (C) 2009 Sascha Kohlmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sportics.dni.rt.client.microedition.controller;


import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;

import net.sportics.dni.rt.client.microedition.ConfigManager;
import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 * The controller is also a bean which contains the login name and
 * the password of the user. After construction login and password
 * may not {@code null}. If login and/or password are {@code null}
 * call {@link #handle(javax.microedition.midlet.MIDlet)}. The user
 * will prompt to insert login and password. Its also possible to
 * change the password if login and/or password are not {@code null},
 * but it's not possible to change the login.
 * @author Sascha Kohlmann
 */
public final class UserController extends AbstractController {

    private static final LogManager LOG = LogManager.getInstance("UserController");
    static {
        LOG.debug("#class: " + LogManager.class.getName());
    }

    static final String LOGIN_ID_KEY = "UserController.login";
    static final String PASSWORD_KEY = "UserController.password";
    private static final String LOGIN_TEXT = "Login";
    private static final int MAX_FIELD_LENGTH = 20;

    private Command selected = null;

    private String login = null;
    private String password = null;
    private boolean passwordChanged = false;

    /**
     * Constructs a new instance.
     * @throws SporticsException if it is not possible to construct a new instance
     */
    public UserController() throws SporticsException {
        super();
        final ConfigManager cfgMgr = ConfigManager.getInstance();
        this.login = cfgMgr.get(LOGIN_ID_KEY);
        this.password = cfgMgr.get(PASSWORD_KEY);
//        this.password = "test";
    }

    protected void doHandle() throws SporticsException {
        boolean loop = true;
        while(loop) {
            final Form form = createForm();
            display(form);
            waitForStateChange();

            if (this.selected == MainController.NEXT) {
                if (this.login == null || this.login.trim().length() == 0) {
                    LOG.debug("BACK: no login");
                    final Alert alert = new Alert("No Login",
                                                  "Please enter a login.",
                                                  null,
                                                  AlertType.INFO);
                    display(alert, form);
                } else if (this.password == null || this.password.trim().length() == 0) {
                    LOG.debug("BACK: no password");
                    final Alert alert = new Alert("No Password",
                                                  "Please enter a password.",
                                                  null,
                                                  AlertType.INFO);
                    display(alert, form);
                } else {
                    LOG.debug("BACK - password: " + this.password + " login: " + this.login);
                    final ConfigManager cfgMgr = ConfigManager.getInstance();
                    cfgMgr.set(LOGIN_ID_KEY, this.login);
                    if (passwordChanged) {
                        cfgMgr.set(PASSWORD_KEY, this.password);
                        LOG.debug("Password changed and stored");
                    } else {
                        LOG.debug("Password NOT changed");
                    }
                    loop = false;
                }
            }
        }
    }

    final void display(final Alert alert, final Displayable displayable) {
        if (displayable == null) {
            throw new IllegalArgumentException("displayble is null");
        }
        if (alert == null) {
            throw new IllegalArgumentException("alert is null");
        }
        final MIDlet m = getMIDlet();
        if (m == null) {
            throw new IllegalStateException("midlet is null");
        }
        final Display display = Display.getDisplay(m);
        alert.setTimeout(Alert.FOREVER);
        display.setCurrent(alert, displayable);
    }

    final Form createForm() {
        final Form form = new Form("User data");
        form.addCommand(MainController.NEXT);
        final TextField loginField = new TextField(LOGIN_TEXT,
                                                   this.login,
                                                   MAX_FIELD_LENGTH,
                                                   TextField.ANY);
        form.append(loginField);

        final TextField passwordField;
        if (this.password == null || this.password.length() == 0) {
            passwordField = new TextField("Password",
                                          this.password,
                                          MAX_FIELD_LENGTH,
                                          TextField.ANY);
        } else {
            passwordField = new TextField("Password",
                                          this.password,
                                          MAX_FIELD_LENGTH,
                                          TextField.ANY | TextField.PASSWORD);
        }

        form.append(passwordField);

        form.setCommandListener(new CommandListener() {
            public void commandAction(final Command cmd, final Displayable displayable) {
                LOG.debug("#class: " + this.getClass().getName());
                UserController.this.selected = cmd;
                UserController.this.login = loginField.getString();
                final String np = passwordField.getString();
                if (np != null) {
                    LOG.debug("pwd from field is not null");
                    if (!np.equals(UserController.this.password)) {
                        LOG.debug("old pwd: " + UserController.this.password + " - new pwd: " + np);
                        UserController.this.password = passwordField.getString();
                        UserController.this.passwordChanged = true;
                    }
                }
                notifyStateChanged();
            }
        });
        form.setItemStateListener(new ItemStateListener() {
            private boolean pwdChanged = false;
            public void itemStateChanged(final Item item) {
                if (item == passwordField && !pwdChanged && UserController.this.password != null) {
                    this.pwdChanged = true;
                    UserController.this.passwordChanged = true;
                    final String pwd = passwordField.getString();
                    LOG.debug("old Pwd: " + pwd);
                    final int length = pwd.length();
                    final String newPwd;
                    if (UserController.this.password.length() < length) {
                        newPwd = pwd.substring(length - 1);
                    } else {
                        newPwd = "";
                    }
                    LOG.debug("new Pwd: " + newPwd);
                    passwordField.setString(newPwd);
                    passwordField.setConstraints(TextField.ANY | TextField.NON_PREDICTIVE);
                }
            }
        });
        return form;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }
}
