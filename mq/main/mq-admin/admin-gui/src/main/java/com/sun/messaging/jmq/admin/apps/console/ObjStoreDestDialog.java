/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)ObjStoreDestDialog.java	1.15 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;
import com.sun.messaging.jmq.admin.apps.console.util.LabelledComponent;
import com.sun.messaging.jmq.admin.apps.console.util.LabelValuePanel;

/** 
 * This dialog is used for object store attributes.
 * It can be used to Add an object store to the list
 * or to modify (update) an existing object store.
 *
 */
public class ObjStoreDestDialog extends AdminDialog {
    
    protected JTextField	lookupText;
    protected JLabel     	lookupLabel;
    protected JCheckBox		checkBox;
    protected JLabel     	destLabel;
    protected JRadioButton      queueButton, topicButton;
    protected JTextField        textItems[];

    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();

    /**
     * Creates a non-modal dialog using the specified frame as parent and string
     * as title. By default, will contain the following buttons:
     * <UL>
     * <LI>OK
     * <LI>CANCEL
     * <LI>HELP
     * </UL>
     *
     * @param parent the Frame from which the dialog is displayed
     * @param title the String to display in the dialog's title bar
     */
    public ObjStoreDestDialog(Frame parent, String title)  {
	super(parent, title, (OK | CANCEL | HELP));
    }

    public ObjStoreDestDialog(Frame parent, String title, int whichButtons)  {
	super(parent, title, whichButtons);
    }

    public JPanel createWorkPanel()  {

	boolean propsDlg = false;
	if (getTitle().equals(acr.getString(acr.I_OBJSTORE_DEST_PROPS))) {
	    propsDlg = true;
        }

	JPanel workPanel = new JPanel();
	GridBagLayout gridbag1 = new GridBagLayout();
	workPanel.setLayout(gridbag1);
	GridBagConstraints c1 = new GridBagConstraints();
	
	JPanel panel1 = new JPanel(new GridLayout(0, 1, -1, -1));

	if (!propsDlg) {
	    JLabel lookUpDescription1 = new JLabel(acr.getString(acr.I_OBJSTORE_JNDI_INFO1));
	    JLabel lookUpDescription2 = new JLabel(acr.getString(acr.I_OBJSTORE_JNDI_INFO2));
	    JLabel lookUpDescription3 = new JLabel(acr.getString(acr.I_OBJSTORE_JNDI_INFO3));
	    //panel1.add(lookUpDescription1);
	    //panel1.add(lookUpDescription2);
	    //panel1.add(lookUpDescription3);
	}
	

	JPanel panel2 = null;
	if (!propsDlg) {
	    panel2 = new JPanel();
	    GridBagLayout gridbag = new GridBagLayout();
	    panel2.setLayout(gridbag);
 
	    GridBagConstraints c = new GridBagConstraints();
	    c.gridx = 0;
	    c.gridy = 0;
	    c.ipadx = 0;
	    c.ipady = -8;
	    c.anchor = GridBagConstraints.WEST;
	    queueButton = new JRadioButton(acr.getString(acr.I_QUEUE), true);
	    gridbag.setConstraints(queueButton, c);
	    panel2.add(queueButton);

	    c.gridx = 0;
	    c.gridy = 1;
	    c.ipadx = 0;
	    c.ipady = 8;
	    c.anchor = GridBagConstraints.WEST;
	    topicButton = new JRadioButton(acr.getString(acr.I_TOPIC));
	    gridbag.setConstraints(topicButton, c);
	    panel2.add(topicButton);

	    ButtonGroup group = new ButtonGroup();
	    group.add(queueButton);
	    group.add(topicButton);
	}

	LabelledComponent items[] = new LabelledComponent[3];
	checkBox = new JCheckBox();
	if (propsDlg) {
	    lookupLabel = new JLabel(" ");
	    destLabel = new JLabel(" ");
	    items[0] = new LabelledComponent(acr.getString(acr.I_OBJSTORE_LOOKUP_NAME) + 
						":", lookupLabel);
	    items[1] = new LabelledComponent(acr.getString(acr.I_OBJSTORE_DEST_TYPE) + 
						":", destLabel);
	} else {
	    lookupText = new JTextField(25);
	    items[0] = new LabelledComponent(acr.getString(acr.I_OBJSTORE_LOOKUP_NAME) +
						":", lookupText);
	    items[1] = new LabelledComponent(acr.getString(acr.I_OBJSTORE_DEST_TYPE) +
					":", panel2, LabelledComponent.NORTH);
	}
	items[2] = new LabelledComponent(acr.getString(acr.I_READONLY) + ":",
				         checkBox);

	LabelValuePanel lvp = new LabelValuePanel(items, 5, 5);

	c1.gridx = 0;
	c1.gridy = 0;
	c1.anchor = GridBagConstraints.NORTHWEST;
	gridbag1.setConstraints(panel1, c1);
	workPanel.add(panel1);

	c1.gridx = 0;
	c1.gridy = 1;
	c1.anchor = GridBagConstraints.WEST;
	gridbag1.setConstraints(lvp, c1);
	workPanel.add(lvp);

	JSeparator separator = new JSeparator();
	c1.gridx = 0;
	c1.gridy = 2;
	c1.ipady = 0; // reset
	c1.anchor = GridBagConstraints.CENTER; // reset
	c1.fill = GridBagConstraints.HORIZONTAL;
	c1.insets = new Insets(5, 0, 5, 0);
	gridbag1.setConstraints(separator, c1);
	workPanel.add(separator);
	
	/*
	 * From here, list the properties on the destination.
	 */
	AdministeredObject obj = (AdministeredObject)new com.sun.messaging.Topic();
	Properties props = obj.getConfiguration();

	LabelledComponent items2[] = new LabelledComponent[props.size()];
	textItems = new JTextField[props.size()];

	int i = 0;
	for (Enumeration e = obj.enumeratePropertyNames(); e.hasMoreElements(); i++) {
	    String propName = (String)e.nextElement();
	    try {
		textItems[i] = new JTextField((String)props.get(propName), 25);
		items2[i] = new LabelledComponent(obj.getPropertyLabel(propName) + ":",
							textItems[i]);
	    } catch (Exception ex) {
	    }
	}

	LabelValuePanel lvp2 = new LabelValuePanel(items2, 5, 5);

	c1.gridx = 0;
	c1.gridy = 3;
	c1.anchor = GridBagConstraints.WEST;
	gridbag1.setConstraints(lvp2, c1);
	workPanel.add(lvp2);

	// Set width lookup name label to max width of bottom panel.
	if (propsDlg) {
	    int maxWidth = lvp2.getPreferredSize().width;
	    JComponent c = items[0].getComponent();
	    Dimension dim = new Dimension(maxWidth - items[0].getLabelWidth() - 20, 
					  c.getPreferredSize().height);
	    c.setPreferredSize(dim);
	}

	return (workPanel);
    }

    public void doOK()  { }
    public void doApply()  { }
    public void doReset() { }
    public void doCancel() { }
    public void doClose() { }
    public void doClear() { }

}
