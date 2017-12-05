/*
 * The MIT License
 *
 * Copyright 2017 Focused Ultrasound Foundation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.fusfoundation.kranion.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.fusfoundation.kranion.Button;
import org.fusfoundation.kranion.ProgressListener;
import org.fusfoundation.kranion.Renderable;
import org.fusfoundation.kranion.FlyoutPanel;
import org.fusfoundation.kranion.plugin.Plugin;


import org.fusfoundation.kranion.view.View;
import org.fusfoundation.kranion.model.*;
import org.fusfoundation.kranion.controller.Controller;
import org.fusfoundation.kranion.model.image.ImageVolume;
import org.fusfoundation.kranion.model.image.ImageVolume4D;

import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author john
 */
public class HydrophoneSkullPlugin implements Plugin, ActionListener {
    private Model model;
    private View view;
    private Controller controller;
    private FiducialFinder fiducialFinder = new FiducialFinder();
    
    @Override
    public String getName() {
        return "HydrophoneSkullPlugin";
    }
    
    @Override
    public void init(Controller controller) {
        this.model = controller.getModel();
        this.view = controller.getView();
        this.controller = controller;

        System.out.println("******* Hello from HydrophoneSkullPlugin !! ****************");

        controller.addActionListener(this);

        Renderable mainPanel = Renderable.lookupByTag("MainFlyout");
        if (mainPanel != null && mainPanel instanceof FlyoutPanel) {
            FlyoutPanel panel = (FlyoutPanel) mainPanel;
            panel.addChild("HydrophoneFiducials", new Button(Button.ButtonType.BUTTON, 10, 240, 220, 25, controller).setTitle("Find Fiducials").setCommand("findFiducials"));
            panel.addChild("HydrophoneFiducials", new Button(Button.ButtonType.BUTTON, 10, 190, 200, 25, controller).setTitle("Show my loc.").setCommand("ShowMyLoc"));

//        // to move the transducer on X axis
            panel.addChild("HydrophoneFiducials", new Button(Button.ButtonType.BUTTON, 10, 140, 100, 35, controller).setTitle(" Tx +X").setCommand("TxShiftToPlusDirX"));
            panel.addChild("HydrophoneFiducials", new Button(Button.ButtonType.BUTTON, 10, 100, 100, 35, controller).setTitle(" Tx -X").setCommand("TxShiftToMinusDirX"));
//        // to move the transducer on Y axis
            panel.addChild("HydrophoneFiducials", new Button(Button.ButtonType.BUTTON, 120, 140, 100, 35, controller).setTitle(" Tx +Y").setCommand("TxShiftToPlusDirY"));
            panel.addChild("HydrophoneFiducials", new Button(Button.ButtonType.BUTTON, 120, 100, 100, 35, controller).setTitle(" Tx -Y").setCommand("TxShiftToMinusDirY"));
//        // to move the transducer on Z axis
            panel.addChild("HydrophoneFiducials", new Button(Button.ButtonType.BUTTON, 230, 140, 100, 35, controller).setTitle(" Tx +Z").setCommand("TxShiftToPlusDirZ"));
            panel.addChild("HydrophoneFiducials", new Button(Button.ButtonType.BUTTON, 230, 100, 100, 35, controller).setTitle(" Tx -Z").setCommand("TxShiftToMinusDirZ"));
        }
    }
    
    @Override
    public void release() {
        controller = null;
        model = null;
        view = null;
        
        controller.removeActionListener(this);        
    }    

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("findFiducials")) {
            ImageVolume4D image = (ImageVolume4D)model.getCtImage();
            if (image != null && model != null && view != null) {
                if (fiducialFinder.find(image, controller)) {               
                     model.setCtImage(image);
                     model.setAttribute("currentTargetPoint", new Vector3f());
                     view.setIsDirty(true);
                }
            }
        }                
        else if (e.getActionCommand().equals("ShowMyLoc")) {
            try {

                System.out.println(" ");
                System.out.println("** My Location is: ");

                try {
                    Vector3f transCT = (Vector3f) model.getCtImage().getAttribute("ImageTranslation");
                    System.out.println("** CT: X axis: " + transCT.x + ",Y axis: " + transCT.y + ", Z axis: " + transCT.z);
                } catch (Exception ect) {
                    System.out.println("\t No CT found.");
                    //ect.printStackTrace();
                }

                try {
                    Vector3f transMR = (Vector3f) model.getMrImage(0).getAttribute("ImageTranslation");
                    System.out.println("** MR: X axis: " + transMR.x + ",Y axis: " + transMR.y + ", Z axis: " + transMR.z);
                } catch (Exception emr) {
                    System.out.println("\t No MR found.");
                    //emr.printStackTrace();
                }
                System.out.println(" ");

            } catch (Exception ei) {
                ei.printStackTrace();
            }
            
        }
        else if (e.getActionCommand().equals("TxShiftToPlusDirX")) {
  
            shiftMRCT( 1, 10); // shift 1 = 1mm  
        }
                
        else if (e.getActionCommand().equals("TxShiftToMinusDirX")) {             
              
            shiftMRCT( 1, -10);
        }
        else if (e.getActionCommand().equals("TxShiftToPlusDirY")) {
  
            shiftMRCT( 2, 10);   
        }
                
        else if (e.getActionCommand().equals("TxShiftToMinusDirY")) {             
              
            shiftMRCT( 2, -10);
        }
        else if (e.getActionCommand().equals("TxShiftToPlusDirZ")) {
  
            shiftMRCT( 3, 10);   
        }
                
        else if (e.getActionCommand().equals("TxShiftToMinusDirZ")) {             
              
            shiftMRCT( 3, -10);
        }
    }
    
    private void shiftMRCT(int AxisNumb, float shiftValue) {
        try{
            ImageVolume ctimage = model.getCtImage();
            ImageVolume mrimage = model.getMrImage(0);
            
            Vector3f transCT = new Vector3f();
            if (ctimage != null) {
                transCT = (Vector3f)ctimage.getAttribute("ImageTranslation");
            }
            Vector3f transMR = new Vector3f();
            if (mrimage != null) {
                transMR = (Vector3f)mrimage.getAttribute("ImageTranslation");
            }

            if (AxisNumb == 1){
                transCT.x += shiftValue;
                transMR.x += shiftValue;
            }else if(AxisNumb ==2){
                transCT.y += shiftValue;
                transMR.y += shiftValue;
            }else if(AxisNumb ==3){
                transCT.z += shiftValue;
                transMR.z += shiftValue;
            }

            if (ctimage != null) {
                ctimage.setAttribute("ImageTranslation", transCT);
            }
           
            if (mrimage != null) {
                mrimage.setAttribute("ImageTranslation", transMR);
            }
            
            System.out.println(" "); 
            System.out.println("** CT: X axis: " + transCT.x + ",Y axis: " + transCT.y + ", Z axis: " +transCT.z);
            System.out.println("** MR: X axis: " + transMR.x + ",Y axis: " + transMR.y + ", Z axis: " +transMR.z);
            System.out.println(" ");
            
            view.setIsDirty(true);
            
        }
        catch(Exception ei) {
            ei.printStackTrace();
        }
    }
}
