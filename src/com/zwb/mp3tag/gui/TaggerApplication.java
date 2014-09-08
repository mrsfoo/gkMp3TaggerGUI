package com.zwb.mp3tag.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.jmx.snmp.Timestamp;
import com.zwb.fsparser.api.GkFsParserFactory;
import com.zwb.fsparser.api.IGkFsEntry;
import com.zwb.fsparser.api.IGkFsParser;
import com.zwb.fsparser.api.IGkFsParserResult;
import com.zwb.fsparser.api.IGkFsParserSearchLocation;
import com.zwb.mp3tag.connector.api.GkMp3TaggerAggregatorFactory;
import com.zwb.mp3tag.connector.api.IGkMp3TagWriter;
import com.zwb.mp3tag.connector.api.IGkMp3TaggerMetaInfoCollector;
import com.zwb.mp3tag.connector.exception.GkMp3TaggerConnectorExceptionTagger;
import com.zwb.mp3tag.gui.util.Config;
import com.zwb.mp3tag.gui.util.MyLogger;

public class TaggerApplication
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
    private JPanel buttonPanel;
    
    private MyLogger log = new MyLogger(this.getClass());
    IGkMp3TagWriter writer = GkMp3TaggerAggregatorFactory.createTagWriter();
    IGkMp3TaggerMetaInfoCollector collector = GkMp3TaggerAggregatorFactory.createMetaInfoCollector();
    IGkFsParser fsParser = GkFsParserFactory.createParser();
    
    public TaggerApplication()
    {
	createMainWindow();
    }
    
    private void createMainWindow()
    {
	int rows = 0;
	mainFrame = new JFrame(Config.MAIN_WINDOW_TITLE);
	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	mainFrame.setSize(750, 300);
	mainFrame.setLayout(new BorderLayout());
	mainFrame.setBackground(Color.GREEN);
	
	this.fileChooser = new JFileChooser(Config.getDefaultFolder());
	this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	this.fileChooser.addActionListener(new FileSelectorActionListener());
	this.fileChooser.setControlButtonsAreShown(false);
	// this.fileChooser.setAccessory(new FileSelectorAccessory());
	mainFrame.add(this.fileChooser, BorderLayout.WEST);
	this.fileChooser.addPropertyChangeListener(new FileSelectorAccessoryActionListener());
	
	this.artistReleasePanel = new JPanel();
	this.artistReleasePanel.setLayout(new FlowLayout());
	this.artistReleasePanel.setBackground(Color.YELLOW);
	
	this.labelArtist = new JLabel(Config.LABEL_ARTIST);
	this.artistReleasePanel.add(this.labelArtist);
	
	this.labelRelease = new JLabel(Config.LABEL_RELEASE);
	this.artistReleasePanel.add(this.labelRelease);
	
	this.mainFrame.add(this.artistReleasePanel, BorderLayout.SOUTH);
	
	this.buttonPanel = new JPanel();
	this.buttonPanel.setLayout(new GridLayout(rows, 1));
	
	this.buttonParse = new JButton(Config.BUTTON_PARSE_TITLE);
	this.buttonParse.addActionListener(new ParseButtonAL());
	this.buttonPanel.add(this.buttonParse);
	rows++;
	
	this.buttonTag = new JButton(Config.BUTTON_TAG_TITLE);
	this.buttonTag.addActionListener(new TagButtonActionListener());
	this.buttonPanel.add(this.buttonTag);
	rows++;
	
	this.buttonShowMetainfo = new JButton(Config.BUTTON_SHOW_META_TITLE);
	this.buttonShowMetainfo.addActionListener(new ShowButtonActionListener());
	this.buttonPanel.add(this.buttonShowMetainfo);
	rows++;
	
	this.checkBoxFormat = new JCheckBox(Config.CHECKBOX_FORMAT_TITLE);
	this.checkBoxFormat.setEnabled(true);
	this.buttonPanel.add(this.checkBoxFormat);
	rows++;
	
	this.mainFrame.add(this.buttonPanel, BorderLayout.CENTER);
	
	mainFrame.pack();
	mainFrame.setVisible(true);
    }
    
    private IGkFsEntry getLocation(String command)
    {
	File f = this.fileChooser.getSelectedFile();
	if (f != null)
	{
	    log.debug(command + " with selected folder: " + f.getAbsolutePath());
	    IGkFsParserSearchLocation searchLoc = GkFsParserFactory.createSearchLocation("<foo>", f.getAbsolutePath(), 0);
	    IGkFsParserResult result = this.fsParser.parseFolders(searchLoc);
	    if (result.getEntries().size() != 1)
	    {
		log.error("folder search results [" + result.getEntries().size() + "]" + result.getEntries());
		updateArtistReleaseUnapplicable();
		return null;
	    }
	    IGkFsEntry res = result.getEntries().get(0);
	    updateArtistRelease(res);
	    return res;
	}
	log.error(command + " with selected folder: " + f);
	updateArtistReleaseUnapplicable();
	// TODO: show error window
	return null;
    }
    
    private void updateArtistRelease(IGkFsEntry loc)
    {
	this.labelArtist.setText(Config.LABEL_ARTIST + "<" + loc.getArtistName() + ">");
	this.labelRelease.setText(Config.LABEL_RELEASE + "<" + loc.getReleaseName() + ">");
    }
    
    private void updateArtistReleaseUnapplicable()
    {
	this.labelArtist.setText(Config.LABEL_ARTIST + "--");
	this.labelRelease.setText(Config.LABEL_RELEASE + "--");
    }
    
    private void parseMetaInfo(IGkFsEntry entry)
    {
	this.collector.setSimpleFormat(this.checkBoxFormat.isEnabled());
	this.collector.collectMetaInfo(entry.getPath());
    }
    
    private void writeTags(IGkFsEntry entry)
    {
	try
	{
	    this.writer.writeTags(entry.getPath());
	}
	catch (GkMp3TaggerConnectorExceptionTagger e)
	{
	    // TODO: show error window
	}
    }
    
    class ParseButtonAL implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    IGkFsEntry e = TaggerApplication.this.getLocation(a.getActionCommand());
	    if (e != null)
	    {
		TaggerApplication.this.parseMetaInfo(e);
	    }
	    else
	    {// TODO: show error window
	    
	    }
	}
    }
    
    class TagButtonActionListener implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    IGkFsEntry e = TaggerApplication.this.getLocation(a.getActionCommand());
	    if (e != null)
	    {
		TaggerApplication.this.writeTags(e);
	    }
	    else
	    {// TODO: show error window
	    }
	}
    }
    
    class ShowButtonActionListener implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    IGkFsEntry e = TaggerApplication.this.getLocation(a.getActionCommand());
	}
    }
    
    class FileSelectorActionListener implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    IGkFsEntry e = TaggerApplication.this.getLocation(a.getActionCommand());
	}
    }
    
    class FileSelectorAccessoryActionListener implements PropertyChangeListener
    {
	public void propertyChange(PropertyChangeEvent a)
	{
	    if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(a.getPropertyName()))
	    {
		IGkFsEntry e = TaggerApplication.this.getLocation(a.getPropertyName());
	    }
	}
    }
    
}
