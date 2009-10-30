/* Copyright (C) 2008-2009 Sascha Kohlmann
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

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import net.sportics.dni.rt.client.microedition.SporticsException;

public final class BooleanSelectionController extends AbstractController {

    private String title = null;
    private String question = null;
    private boolean newDecision = false;

    private Command selected = null;

    protected void doHandle() throws SporticsException {
        final ChoiceGroup cg = new ChoiceGroup(this.question, ChoiceGroup.EXCLUSIVE);
        cg.append("Yes", null);
        cg.append("No", null);
        if (this.newDecision) {
            cg.setSelectedIndex(0, true);
            cg.setSelectedIndex(1, false);
        } else {
            cg.setSelectedIndex(0, false);
            cg.setSelectedIndex(1, true);
        }
        final Form form = new Form(this.title);
        form.addCommand(MainController.BACK);
        form.setCommandListener(new CommandListener() {
            public void commandAction(final Command cmd, final Displayable displayable) {
                BooleanSelectionController.this.selected = cmd;
                notifyStateChanged();
            }
        });
        form.append(cg);
        display(form);
        waitForStateChange();
        while(true) {
            if (this.selected == MainController.BACK) {
                if (cg.getSelectedIndex() == 0) {
                    this.newDecision = true;
                } else {
                    this.newDecision = false;
                }
                return;
            }
        }
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public boolean getNewDecision() {
        return this.newDecision;
    }

    public boolean getCurrentDecision() {
        return this.newDecision;
    }

    public void setCurrentDecision(final boolean current) {
        this.newDecision = current;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
}
