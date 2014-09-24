package com.zwb.mp3tag.gui.util;

import java.io.File;

import com.zwb.config.api.ConfigurationFactory;
import com.zwb.config.api.IConfiguration;

public class Config
{
    public static final String MAIN_WINDOW_TITLE = "geekOlogy mp3 tagger";
    public static final String BUTTON_PARSE_TITLE = "parse internet for metadata";
    public static final String BUTTON_TAG_TITLE = "tag with metadata";
    public static final String CHECKBOX_FORMAT_TITLE = "simple format?";
    public static final String BUTTON_SHOW_META_TITLE = "show metadata file (external editor)";
    public static final String LABEL_ARTIST = "artist: ";
    public static final String LABEL_RELEASE = "release: ";
    public static final String LABEL_EDITOR_PATH = "external editor command:";
    
    public static final String EDITOR_FILE_PLACEHOLDER = "\\$FOLDER_PATH";
    
    public static final String PERSISTED_FOLDER_PROPERTY = "geekology.mp3.tagger.gui.folder";
    
    private static final String CONFIG_NAME = "mp3.tagger.gui.config";
    private static final String CONFIG_KEY_DEFAULT_FOLDER_WIN = "default.folder.win";
    private static final String CONFIG_KEY_DEFAULT_EDITOR_WIN = "default.editor.win";
    
    private static IConfiguration config = ConfigurationFactory.getBufferedConfiguration(CONFIG_NAME);
    
    private static final String SYSPROP_OS_NAME = "os.name";
    private static final String SYSPROP_OS_NAME_WIN = "windows";
    private static final String SYSPROP_OS_NAME_LINUX = "linux";
    
    public static File getDefaultFolder()
    {
	String os = System.getProperties().getProperty(SYSPROP_OS_NAME);
	if (os == null)
	{
	    throw new IllegalArgumentException("cannot determine OS");
	}
	String path;
	if (os.toLowerCase().startsWith(SYSPROP_OS_NAME_WIN))
	{
	    path = config.getString(CONFIG_KEY_DEFAULT_FOLDER_WIN, "");
	}
	else if (os.toLowerCase().startsWith(SYSPROP_OS_NAME_LINUX))
	{
	    throw new IllegalArgumentException("reading of this config value for OS Linux not implemented");
	}
	else
	{
	    throw new IllegalArgumentException("reading of this config value for OS <" + os + "> not implemented");
	}
	if (!path.isEmpty())
	{
	    File f = new File(path);
	    if (f.isDirectory() && f.exists() && f.canWrite())
	    {
		return new File(path);
	    }
	}
	return File.listRoots()[0];
    }
    
    public static String getDefaultEditorPath()
    {
	String os = System.getProperties().getProperty(SYSPROP_OS_NAME);
	if (os == null)
	{
	    throw new IllegalArgumentException("cannot determine OS");
	}
	if (os.toLowerCase().startsWith(SYSPROP_OS_NAME_WIN))
	{
	    return config.getString(CONFIG_KEY_DEFAULT_EDITOR_WIN, "");
	}
	else if (os.toLowerCase().startsWith(SYSPROP_OS_NAME_LINUX))
	{
	    throw new IllegalArgumentException("reading of this config value for OS Linux not implemented");
	}
	else
	{
	    throw new IllegalArgumentException("reading of this config value for OS <" + os + "> not implemented");
	}
    }
}
