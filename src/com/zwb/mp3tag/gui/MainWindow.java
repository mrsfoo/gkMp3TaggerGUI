package com.zwb.mp3tag.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainWindow
{
    private JFrame mainFrame;
    private JButton buttonParse;
    private JButton buttonTag;
    private JButton buttonShowMetainfo;
    private JCheckBox checkBoxFormat;
    private JFileChooser fileChooser;
    private JLabel labelArtist;
    private JLabel labelRelease;
    private JPanel artistReleasePanel;
    
    public MainWindow()
    {
	createMainWindow();
    }
    
    private void createMainWindow()
    {
	mainFrame = new JFrame(Config.MAIN_WINDOW_TITLE);
	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	mainFrame.setSize(750, 300);
	mainFrame.setLayout(new FlowLayout());
	mainFrame.setBackground(Color.GREEN);
	
	this.fileChooser = new JFileChooser(File.listRoots()[0]);
	this.fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
	mainFrame.add(this.fileChooser);
	this.fileChooser.addActionListener(new FileSelectorAL());
	
	this.artistReleasePanel = new JPanel();
	this.artistReleasePanel.setLayout(new FlowLayout());
	this.artistReleasePanel.setBackground(Color.YELLOW);
	
	this.labelArtist = new JLabel(Config.LABEL_ARTIST);
	this.artistReleasePanel.add(this.labelArtist);
	
	this.labelRelease = new JLabel(Config.LABEL_RELEASE);
	this.artistReleasePanel.add(this.labelRelease);

	this.mainFrame.add(this.artistReleasePanel);
	
	this.buttonParse = new JButton(Config.BUTTON_PARSE_TITLE);
	this.buttonParse.addActionListener(new ParseButtonAL());
	mainFrame.add(this.buttonParse);

	this.buttonTag = new JButton(Config.BUTTON_TAG_TITLE);
	this.buttonTag.addActionListener(new TagButtonAL());
	mainFrame.add(this.buttonTag);

	this.buttonShowMetainfo = new JButton(Config.BUTTON_SHOW_META_TITLE);
	this.buttonShowMetainfo.addActionListener(new ShowButtonAL());
	mainFrame.add(this.buttonShowMetainfo);

	this.checkBoxFormat = new JCheckBox(Config.CHECKBOX_FORMAT_TITLE);
	mainFrame.add(this.checkBoxFormat);

	mainFrame.pack();
	mainFrame.setVisible(true);
    }
    
    class ParseButtonAL implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    System.out.println(MainWindow.this.fileChooser.getSelectedFile().getAbsolutePath());
	}
    }
    
    class TagButtonAL implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    System.out.println(MainWindow.this.fileChooser.getSelectedFile().getAbsolutePath());
	}
    }
    
    class ShowButtonAL implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    System.out.println(MainWindow.this.fileChooser.getSelectedFile().getAbsolutePath());
	}
    }

    class FileSelectorAL implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    System.out.println(MainWindow.this.fileChooser.getSelectedFile().getAbsolutePath());
	    File f = MainWindow.this.fileChooser.getSelectedFile();
	    if(f!=null)
	    {
		MainWindow.this.labelArtist.setText(Config.LABEL_ARTIST + f.getName());
		MainWindow.this.labelRelease.setText(Config.LABEL_RELEASE + f.getName());
	    }
	}
    }
    
}
