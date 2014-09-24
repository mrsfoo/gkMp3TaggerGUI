package com.zwb.mp3tag.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

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
    private JPanel editorPanel;
    private JPanel editorSubPanel;
    private JTextField textFieldEditorPath;
    private JLabel labelFieldEditorPath;
    private JTextArea textArea;
    private JPanel statusPanel;
    
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
	// mainFrame.setSize(750, 50);
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
	this.textFieldEditorPath = new JTextField();
	this.textFieldEditorPath.setText(Config.getDefaultEditorPath());
	this.labelFieldEditorPath = new JLabel(Config.LABEL_EDITOR_PATH);
	this.editorPanel = new JPanel();
	this.editorPanel.setLayout(new GridLayout(1, 2));
	this.editorPanel.add(this.buttonShowMetainfo);
	this.editorSubPanel = new JPanel();
	this.editorSubPanel.setLayout(new GridLayout(2, 1));
	this.editorSubPanel.add(this.labelFieldEditorPath);
	this.editorSubPanel.add(this.textFieldEditorPath);
	this.editorPanel.add(this.editorSubPanel);
	this.buttonPanel.add(editorPanel);
	rows++;
	
	this.checkBoxFormat = new JCheckBox(Config.CHECKBOX_FORMAT_TITLE);
	this.checkBoxFormat.setSelected(true);
	this.buttonPanel.add(this.checkBoxFormat);
	rows++;
	
	this.mainFrame.add(this.buttonPanel, BorderLayout.CENTER);
	
	mainFrame.pack();
	mainFrame.setVisible(true);
	this.statusPanel = new JPanel();
	this.statusPanel.setLayout(new BorderLayout());
	
	this.labelArtist = new JLabel(Config.LABEL_ARTIST);
	this.artistReleasePanel.add(this.labelArtist);
	
	this.labelRelease = new JLabel(Config.LABEL_RELEASE);
	this.artistReleasePanel.add(this.labelRelease);
	
	this.statusPanel.add(this.artistReleasePanel, BorderLayout.NORTH);
	
	this.textArea = new JTextArea();
	JPanel p = new JPanel();
	JScrollPane scroll = new JScrollPane(textArea);
	int wid = this.mainFrame.getWidth();
	int hig = 200;
	this.textArea.setBounds(0, 0, wid, hig);
	p.setPreferredSize(new Dimension(wid, hig));
	scroll.setBounds(0, 0, wid, hig);
	
	p.setLayout(null);
	p.add(scroll);
	
	this.statusPanel.add(p, BorderLayout.SOUTH);
	this.mainFrame.add(this.statusPanel, BorderLayout.SOUTH);
	
	mainFrame.pack();
	mainFrame.setVisible(true);
    }
    
    private IGkFsEntry getSelectedFolder(String command)
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
		return null;
	    }
	    IGkFsEntry res = result.getEntries().get(0);
	    return res;
	}
	log.error(command + " with selected folder: " + f);
	// TODO: show error window
	return null;
    }
    
    private File getMetadataFile(IGkFsEntry entry)
    {
	File f = new File(entry.getPath(), this.collector.getMetaInfoFilename());
	if (f.exists() && !f.isDirectory() && f.canWrite())
	{
	    return f;
	}
	else
	{
	    log.error("no valid metadata file in folder <" + entry.getPath() + ">");
	    return null;
	}
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
    
    private void showInEditor(IGkFsEntry entry)
    {
	String editorCmdRaw = this.textFieldEditorPath.getText();
	String editorPath = new String(editorCmdRaw);
	editorPath = editorPath.replaceAll("\"", "");
	editorPath = editorPath.replaceFirst(Config.EDITOR_FILE_PLACEHOLDER, "");
	editorPath = editorPath.trim();
	log.debug("editor command    : <" + editorCmdRaw + ">");
	log.debug("editor path       : <" + editorPath + ">");
	
	File metaInfoFile = getMetadataFile(entry);
	if (metaInfoFile == null)
	{
	    // TODO error window zeigen
	    return;
	}
	
	File editor = new File(editorPath);
	if ((editorPath == null) || editorPath.isEmpty() || !editor.exists() || !editor.isFile() || !editor.canExecute())
	{
	    log.error("editor in path <" + editor.getAbsolutePath() + "> is not valid");
	    log.trace("(editorPath == null)   : " + (editorPath == null));
	    log.trace("editorPath.isEmpty()   : " + editorPath.isEmpty());
	    log.trace("!editor.exists()       : " + (!editor.exists()));
	    log.trace("!editor.isFile()       : " + (!editor.isFile()));
	    log.trace("!editor.canExecute()   : " + (!editor.canExecute()));
	    // TODO error window zeigen
	    return;
	}
	
	editorCmdRaw = editorCmdRaw.replaceAll("\"", "");
	String[] tokens = editorCmdRaw.split(Config.EDITOR_FILE_PLACEHOLDER);
	if ((tokens.length > 2) || (tokens.length < 1))
	{
	    log.error("unable to replace target folder placeholder <" + Config.EDITOR_FILE_PLACEHOLDER + "> in raw command " + editorCmdRaw + " (token size is " + tokens.length);
	    // TODO error window zeigen
	    return;
	}
	String command = "\"" + tokens[0].trim() + "\" \"" + metaInfoFile.getAbsolutePath() + "\"";
	if ((tokens.length > 1) && (!tokens[1].isEmpty()))
	{
	    command += "\"" + tokens[1].trim() + "\"";
	}
	log.debug("processed command : <" + command + ">");
	
	log.debug("executing command >>" + command);
	try
	{
	    Runtime.getRuntime().exec(command);
	}
	catch (IOException e)
	{
	    log.error("problem executing editor in path <" + editorPath + "> with comand <" + editorCmdRaw + ">: " + e.getMessage());
	}
    }
    
    class ParseButtonAL implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    IGkFsEntry e = TaggerApplication.this.getSelectedFolder(a.getActionCommand());
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
	    IGkFsEntry e = TaggerApplication.this.getSelectedFolder(a.getActionCommand());
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
	    IGkFsEntry e = TaggerApplication.this.getSelectedFolder(a.getActionCommand());
	    if (e != null)
	    {
		TaggerApplication.this.showInEditor(e);
	    }
	    else
	    {// TODO: show error window
	    }
	}
    }
    
    class FileSelectorActionListener implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    IGkFsEntry e = TaggerApplication.this.getSelectedFolder(a.getActionCommand());
	}
    }
    
    private void actTextToTextAreaFromFile(File f, JTextArea area)
    {
	try
	{
	    
	    String strLine;
	    BufferedReader br = new BufferedReader(new FileReader(f));
	    area.setText("");
	    
	    while ((strLine = br.readLine()) != null)
	    {
		area.append(strLine + "\n");
		
		System.out.println(strLine);
		
	    }
	}
	catch (Exception e)
	{
	    System.err.println("Error: " + e.getMessage());
	}
    }
    
    class FileSelectorAccessoryActionListener implements PropertyChangeListener
    {
	public void propertyChange(PropertyChangeEvent a)
	{
	    if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(a.getPropertyName()))
	    {
		IGkFsEntry e = TaggerApplication.this.getSelectedFolder(a.getPropertyName());
		if (e != null)
		{
		    updateArtistRelease(e);
		    File mdf = getMetadataFile(e);
		    if (mdf != null)
		    {
			TaggerApplication.this.actTextToTextAreaFromFile(mdf, TaggerApplication.this.textArea);
		    }
		    else
		    {
			TaggerApplication.this.textArea.setText("");
		    }
		}
		else
		{
		    TaggerApplication.this.textArea.setText("");
		    updateArtistReleaseUnapplicable();
		}
	    }
	}
    }
    
}
