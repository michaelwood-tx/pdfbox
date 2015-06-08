/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.tools.gui.PDFTreeModel;
import org.apache.pdfbox.tools.gui.PDFTreeCellRenderer;
import org.apache.pdfbox.tools.gui.ArrayEntry;
import org.apache.pdfbox.tools.gui.MapEntry;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.pdfbox.tools.pdfdebugger.colorpane.CSSeparation;
import org.apache.pdfbox.tools.util.FileOpenSaveDialog;
import org.apache.pdfbox.tools.pdfdebugger.ui.Tree;
import org.apache.pdfbox.tools.pdfdebugger.treestatus.TreeStatus;
import org.apache.pdfbox.tools.pdfdebugger.treestatus.TreeStatusPane;
import org.apache.pdfbox.tools.util.RecentFiles;

/**
 *
 * @author wurtz
 * @author Ben Litchfield
 */
public class PDFDebugger extends javax.swing.JFrame
{
    private TreeStatusPane statusPane;
    private RecentFiles recentFiles;

    private PDDocument document = null;
    private String currentFilePath = null;

    private static final Set<COSName> SPECIALCOLORSPACES =
            new HashSet(Arrays.asList(COSName.INDEXED, COSName.SEPARATION, COSName.DEVICEN));

    private static final String PASSWORD = "-password";

    /**
     * Constructor.
     */
    public PDFDebugger()
    {
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()
    {
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new JScrollPane();
        tree = new Tree(this);
        jScrollPane2 = new JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new JMenu();
        openMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        saveAsMenuItem = new JMenuItem();
        recentFilesMenu = new JMenu();
        exitMenuItem = new JMenuItem();
        editMenu = new JMenu();
        cutMenuItem = new JMenuItem();
        copyMenuItem = new JMenuItem();
        pasteMenuItem = new JMenuItem();
        deleteMenuItem = new JMenuItem();
        helpMenu = new JMenu();
        contentsMenuItem = new JMenuItem();
        aboutMenuItem = new JMenuItem();

        tree.setCellRenderer( new PDFTreeCellRenderer() );
        tree.setModel( null );

        setTitle("PDFBox - PDF Debugger");

        addWindowFocusListener(new WindowAdapter()
        {
            @Override
            public void windowGainedFocus(WindowEvent e)
            {
                jScrollPane1.requestFocusInWindow();
                super.windowGainedFocus(e);
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                exitForm(evt);
            }
        });


        jScrollPane1.setBorder(new BevelBorder(BevelBorder.RAISED));
        jScrollPane1.setPreferredSize(new Dimension(300, 500));
        tree.addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent evt)
            {
                jTree1ValueChanged(evt);
            }
        });

        jScrollPane1.setViewportView(tree);

        jSplitPane1.setRightComponent(jScrollPane2);

        jScrollPane2.setPreferredSize(new Dimension(300, 500));
        jScrollPane2.setViewportView(jTextPane1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        JScrollPane documentScroller = new JScrollPane();
        documentScroller.setViewportView( documentPanel );

        statusPane = new TreeStatusPane(tree);
        statusPane.getPanel().setBorder(new BevelBorder(BevelBorder.RAISED));
        statusPane.getPanel().setPreferredSize(new Dimension(300, 25));
        getContentPane().add(statusPane.getPanel(), BorderLayout.PAGE_START);

        getContentPane().add( jSplitPane1, BorderLayout.CENTER );

        fileMenu.setText("File");
        openMenuItem.setText("Open");
        openMenuItem.setToolTipText("Open PDF file");
        openMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        saveMenuItem.setText("Save");

        saveAsMenuItem.setText("Save As ...");

        try
        {
            recentFiles = new RecentFiles(this.getClass(), 5);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        recentFilesMenu.setText("Open recent Files");
        recentFilesMenu.setEnabled(false);
        addRecentFileItems();
        fileMenu.add(recentFilesMenu);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        helpMenu.setText("Help");
        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        setJMenuBar(menuBar);

        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-700)/2, (screenSize.height-600)/2, 700, 600);
    }//GEN-END:initComponents

    private void openMenuItemActionPerformed(ActionEvent evt)
    {
        ExtensionFileFilter pdfFilter = new ExtensionFileFilter(new String[] {"pdf", "PDF"}, "PDF Files");
        FileOpenSaveDialog openDialog = new FileOpenSaveDialog(this, pdfFilter);
        try
        {
            File file = openDialog.openFile();
            if (file != null)
            {
                String name = file.getPath();
                readPDFFile(name, "");
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void jTree1ValueChanged(TreeSelectionEvent evt)
    {
        TreePath path = tree.getSelectionPath();
        if (path != null)
        {
            try
            {
                Object selectedNode = path.getLastPathComponent();
                if (isSpecialColorSpace(selectedNode))
                {
                    showColorPane(selectedNode);
                    return;
                }
                if (!jSplitPane1.getRightComponent().equals(jScrollPane2))
                {
                    jSplitPane1.setRightComponent(jScrollPane2);
                }
                String data = convertToString(selectedNode);
                if (data != null)
                {
                    jTextPane1.setText(data);
                }
                else
                {
                    jTextPane1.setText("");
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }//GEN-LAST:event_jTree1ValueChanged

    private boolean isSpecialColorSpace(Object selectedNode)
    {
        if (selectedNode instanceof MapEntry)
        {
            selectedNode = ((MapEntry) selectedNode).getValue();
        }
        else if (selectedNode instanceof ArrayEntry)
        {
            selectedNode = ((ArrayEntry) selectedNode).getValue();
        }

        if (selectedNode instanceof COSArray)
        {
            COSBase arrayEntry = ((COSArray)selectedNode).get(0);
            if (arrayEntry instanceof COSName)
            {
                COSName name = (COSName) arrayEntry;
                return SPECIALCOLORSPACES.contains(name);
            }
        }
        return false;
    }

    /**
     * Show a Panel describing the special color spaces in more detail and interactive way.
     * For now only Separation Color space is shown.
     * @param csNode the special color space containing node.
     */
    //TODO implement DeviceN and Indexed color spaces related features
    private void showColorPane(Object csNode)
    {
        if (csNode instanceof MapEntry)
        {
            csNode = ((MapEntry) csNode).getValue();
        }
        else if (csNode instanceof ArrayEntry)
        {
            csNode = ((ArrayEntry) csNode).getValue();
        }

        if (csNode instanceof COSArray)
        {
            COSArray array = (COSArray)csNode;
            COSBase arrayEntry = array.get(0);
            if (arrayEntry instanceof COSName)
            {
                COSName csName = (COSName) arrayEntry;
                if (csName.equals(COSName.SEPARATION))
                {
                    jSplitPane1.setRightComponent(new CSSeparation(array).getPanel());
                }
                else
                {
                    if (!jSplitPane1.getRightComponent().equals(jScrollPane2))
                    {
                        jSplitPane1.setRightComponent(jScrollPane2);
                    }
                }
            }
        }
    }

    private String convertToString( Object selectedNode )
    {
        String data = null;
        if(selectedNode instanceof COSBoolean)
        {
            data = "" + ((COSBoolean)selectedNode).getValue();
        }
        else if( selectedNode instanceof COSFloat )
        {
            data = "" + ((COSFloat)selectedNode).floatValue();
        }
        else if( selectedNode instanceof COSNull )
        {
            data = "null";
        }
        else if( selectedNode instanceof COSInteger )
        {
            data = "" + ((COSInteger)selectedNode).intValue();
        }
        else if( selectedNode instanceof COSName )
        {
            data = "" + ((COSName)selectedNode).getName();
        }
        else if( selectedNode instanceof COSString )
        {
            data = "" + ((COSString)selectedNode).getString();
        }
        else if( selectedNode instanceof COSStream )
        {
            try
            {
                COSStream stream = (COSStream)selectedNode;
                InputStream ioStream = stream.getUnfilteredStream();
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int amountRead;
                while( (amountRead = ioStream.read( buffer, 0, buffer.length ) ) != -1 )
                {
                    byteArray.write( buffer, 0, amountRead );
                }
                data = byteArray.toString();
            }
            catch( IOException e )
            {
                throw new RuntimeException(e);
            }
        }
        else if( selectedNode instanceof MapEntry )
        {
            data = convertToString( ((MapEntry)selectedNode).getValue() );
        }
        else if( selectedNode instanceof ArrayEntry )
        {
            data = convertToString( ((ArrayEntry)selectedNode).getValue() );
        }
        return data;
    }

    private void exitMenuItemActionPerformed(ActionEvent evt)
    {
        if( document != null )
        {
            try
            {
                document.close();
                recentFiles.addFile(currentFilePath);
                recentFiles.close();
            }
            catch( IOException e )
            {
                throw new RuntimeException(e);
            }
        }
        System.exit(0);
    }

    /**
     * Exit the Application.
     */
    private void exitForm(java.awt.event.WindowEvent evt)
    {
        if( document != null )
        {
            try
            {
                document.close();
                recentFiles.addFile(currentFilePath);
                recentFiles.close();
            }
            catch( IOException e )
            {
                throw new RuntimeException(e);
            }
        }
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     *
     * @throws Exception If anything goes wrong.
     */
    public static void main(String[] args) throws Exception
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        PDFDebugger viewer = new PDFDebugger();
        String filename = null;
        String password = "";
        for( int i = 0; i < args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            else
            {
                filename = args[i];
            }
        }

        if (filename != null)
        {
            viewer.readPDFFile( filename, password );
        }
        viewer.setVisible(true);
    }

    private void readPDFFile(String filePath, String password) throws Exception
    {
        if( document != null )
        {
            document.close();
            recentFiles.addFile(currentFilePath);
        }
        File file = new File( filePath );
        currentFilePath = file.getPath();
        recentFiles.removeFile(file.getPath());
        parseDocument( file, password );
        TreeStatus treeStatus = new TreeStatus(document.getDocument().getTrailer());
        statusPane.updateTreeStatus(treeStatus);
        TreeModel model=new PDFTreeModel(document);
        tree.setModel(model);
        tree.setSelectionPath(treeStatus.getPathForString("Root"));
        setTitle("PDFBox - " + file.getAbsolutePath());
        addRecentFileItems();
    }
    /**
     * This will parse a document.
     *
     * @param file The file addressing the document.
     *
     * @throws IOException If there is an error parsing the document.
     */
    private void parseDocument( File file, String password )throws IOException
    {
        document = PDDocument.load(file, password);
    }

    private void addRecentFileItems()
    {
        Action recentMenuAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                String filePath = (String) ((JComponent) actionEvent.getSource()).getClientProperty("path");
                try
                {
                    readPDFFile(filePath, "");
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }

            }
        };
        if (!recentFiles.isEmpty())
        {
            recentFilesMenu.removeAll();
            List<String> files = recentFiles.getFiles();
            for (int i = files.size() - 1; i >= 0; i--)
            {
                String path = files.get(i);
                String name = new File(path).getName();
                JMenuItem recentFileMenuItem = new JMenuItem(name);
                recentFileMenuItem.putClientProperty("path", path);
                recentFileMenuItem.addActionListener(recentMenuAction);
                recentFilesMenu.add(recentFileMenuItem);
            }
            recentFilesMenu.setEnabled(true);
        }
    }


    /**
     * This will print out a message telling how to use this utility.
     */
    private static void usage()
    {
        System.err.println(
                "usage: java -jar pdfbox-app-x.y.z.jar PDFDebugger [OPTIONS] <input-file>\n" +
                "  -password <password>      Password to decrypt the document\n" +
                "  <input-file>              The PDF document to be loaded\n"
                );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JMenuItem aboutMenuItem;
    private JMenuItem contentsMenuItem;
    private JMenuItem copyMenuItem;
    private JMenuItem cutMenuItem;
    private JMenuItem deleteMenuItem;
    private JMenu editMenu;
    private JMenuItem exitMenuItem;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenu recentFilesMenu;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextPane jTextPane1;
    private Tree tree;
    private javax.swing.JMenuBar menuBar;
    private JMenuItem openMenuItem;
    private JMenuItem pasteMenuItem;
    private JMenuItem saveAsMenuItem;
    private JMenuItem saveMenuItem;
    private final JPanel documentPanel = new JPanel();
    // End of variables declaration//GEN-END:variables

}
