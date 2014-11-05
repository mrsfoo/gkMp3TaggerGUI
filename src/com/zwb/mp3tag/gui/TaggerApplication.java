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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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
import com.zwb.mp3tag.profile.api.GkTaggingProfileReaderWriterFactory;
import com.zwb.mp3tag.profile.api.IGkTaggingProfileReader;
import com.zwb.mp3tag.profile.api.ITaggingProfile;

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
    private JLabel labelTrackcount;
    private JPanel artistReleasePanel;
    private JPanel buttonPanel;
    private JPanel editorPanel;
    private JPanel editorSubPanel;
    private JTextField textFieldEditorPath;
    private JLabel labelFieldEditorPath;
    private JTextArea textArea;
    private JPanel statusPanel;
    private JPanel statusLine;
    private JLabel statusLabel;
    
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
	this.fileChooser.setMultiSelectionEnabled(true);
	
	this.statusLine = new JPanel();
	this.statusLine.setLayout(new FlowLayout());
	this.statusLine.setBackground(Color.gray);
	this.statusLabel = new JLabel("started...");
	this.statusLine.add(this.statusLabel);
	
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
	
	this.labelTrackcount = new JLabel(Config.LABEL_TRACKCOUNT);
	this.artistReleasePanel.add(this.labelTrackcount);

	this.statusPanel.add(this.statusLine, BorderLayout.EAST);
	this.statusPanel.add(this.artistReleasePanel, BorderLayout.WEST);
	
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
	
	setReadyState();
    }
    
    private List<IGkFsEntry> getSelectedFolder(String command)
    {
	File[] ff = this.fileChooser.getSelectedFiles();
	List<IGkFsEntry> selection = new ArrayList<IGkFsEntry>();
	if (ff != null)
	{
	    for (File f : ff)
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
		selection.add(res);
	    }
	    return selection;
	}
	log.error(command + " with selected folders: " + ff);
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
    
    private void updateArtistReleaseTrackcount(IGkFsEntry loc)
    {
	this.labelArtist.setText(Config.LABEL_ARTIST + "<" + loc.getArtistName() + ">");
	this.labelRelease.setText(Config.LABEL_RELEASE + "<" + loc.getReleaseName() + ">");
	int countInMetaInfo = 0;
	int countInFolder = 0;
	
	File f = loc.getFile();
	for(String s : f.list())
	{
	    if(s.endsWith("mp3"))
	    {
		countInFolder++;
	    }
	}
	IGkTaggingProfileReader reader = GkTaggingProfileReaderWriterFactory.createReader();
	ITaggingProfile profile = reader.read(loc.getPath());
	countInMetaInfo = profile.getTrackInfos().size();
	this.labelTrackcount.setText(Config.LABEL_TRACKCOUNT+ "<MetaInf:" + countInMetaInfo +"/Folder:"+ countInFolder + ">");
    }
    
    private void updateArtistReleaseTrackcountUnapplicable()
    {
	this.labelArtist.setText(Config.LABEL_ARTIST + "--");
	this.labelRelease.setText(Config.LABEL_RELEASE + "--");
	this.labelTrackcount.setText(Config.LABEL_TRACKCOUNT+ "--");
    }
    
    private void updateArtistReleaseTrackcountMulti()
    {
	this.labelArtist.setText(Config.LABEL_ARTIST + "...");
	this.labelRelease.setText(Config.LABEL_RELEASE + "...");
	this.labelTrackcount.setText(Config.LABEL_TRACKCOUNT+ "...");
    }
    
    private void actionBatchParseMetaInfo(List<IGkFsEntry> entries)
    {
	for (IGkFsEntry e : entries)
	{
	    actionParseMetaInfo(e);
	}
    }
    
    private void actionParseMetaInfo(IGkFsEntry entry)
    {
	setRunState("parsing internet for meta info for " + entry.toString());
	this.collector.setSimpleFormat(this.checkBoxFormat.isEnabled());
	this.collector.collectMetaInfo(entry.getPath());
    }
    
    private void actionBatchWriteTags(List<IGkFsEntry> entries)
    {
	for (IGkFsEntry e : entries)
	{
	    actionWriteTags(e);
	}
    }
    
    private void actionWriteTags(IGkFsEntry entry)
    {
	setRunState("writing tags for " + entry.toString());
	try
	{
	    this.writer.writeTags(entry.getPath());
	}
	catch (GkMp3TaggerConnectorExceptionTagger e)
	{
	    // TODO: show error window
	}
    }
    
    private void actionBatchShowInEditor(List<IGkFsEntry> entries)
    {
	for (IGkFsEntry e : entries)
	{
	    actionShowInEditor(e);
	}
    }
    
    private void actionShowInEditor(IGkFsEntry entry)
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
    
    private void setRunState(String stateName)
    {
	this.buttonPanel.setEnabled(false);
	this.buttonParse.setEnabled(false);
	this.buttonShowMetainfo.setEnabled(false);
	this.buttonTag.setEnabled(false);
	this.statusLine.setBackground(Color.BLUE);
	this.statusLabel.setText("processing " + stateName + "...");
    }
    
    private void setErrorState(Exception e)
    {
	this.buttonPanel.setEnabled(true);
	this.buttonParse.setEnabled(true);
	this.buttonShowMetainfo.setEnabled(true);
	this.buttonTag.setEnabled(true);
	this.statusLine.setBackground(Color.RED);
	this.statusLabel.setText("ERROR: " + e.getMessage());
    }
    
    private void setReadyState()
    {
	this.buttonPanel.setEnabled(true);
	this.buttonParse.setEnabled(true);
	this.buttonShowMetainfo.setEnabled(true);
	this.buttonTag.setEnabled(true);
	this.statusLine.setBackground(Color.GREEN);
	this.statusLabel.setText("READY");
    }
    
    class ParseButtonAL implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    List<IGkFsEntry> e = TaggerApplication.this.getSelectedFolder(a.getActionCommand());
	    if (e != null)
	    {
		TaggerApplication.this.actionBatchParseMetaInfo(e);
	    }
	    else
	    {
		// TODO: show error window
	    }
	    setReadyState();
	}
    }
    
    class TagButtonActionListener implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    List<IGkFsEntry> e = TaggerApplication.this.getSelectedFolder(a.getActionCommand());
	    if (e != null)
	    {
		TaggerApplication.this.actionBatchWriteTags(e);
	    }
	    else
	    {
		// TODO: show error window
	    }
	    setReadyState();
	}
    }
    
    class ShowButtonActionListener implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    List<IGkFsEntry> e = TaggerApplication.this.getSelectedFolder(a.getActionCommand());
	    if (e != null)
	    {
		TaggerApplication.this.actionBatchShowInEditor(e);
	    }
	    else
	    {
		// TODO: show error window
	    }
	    setReadyState();
	}
    }
    
    class FileSelectorActionListener implements ActionListener
    {
	@Override
	public void actionPerformed(ActionEvent a)
	{
	    List<IGkFsEntry> e = TaggerApplication.this.getSelectedFolder(a.getActionCommand());
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
		List<IGkFsEntry> e = TaggerApplication.this.getSelectedFolder(a.getPropertyName());
		if (e != null)
		{
		    if (e.size() > 1)
		    {
			updateArtistReleaseTrackcountMulti();
		    }
		    else if (e.size() == 1)
		    {
			updateArtistReleaseTrackcount(e.get(0));
			File mdf = getMetadataFile(e.get(0));
			if (mdf != null)
			{
			    TaggerApplication.this.actTextToTextAreaFromFile(mdf, TaggerApplication.this.textArea);
			}
			else
			{
			    TaggerApplication.this.textArea.setText("");
			}
		    }
		}
		else
		{
		    TaggerApplication.this.textArea.setText("");
		    updateArtistReleaseTrackcountUnapplicable();
		}
	    }
	}
    }
    
}
