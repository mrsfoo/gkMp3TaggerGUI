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
    public static final String BUTTON_SHOW_META_TITLE = "show metadata file";
    public static final String LABEL_ARTIST = "artist: ";
    public static final String LABEL_RELEASE = "release: ";
    
    public static final String PERSISTED_FOLDER_PROPERTY = "geekology.mp3.tagger.gui.folder";
    
    
    private static final String CONFIG_NAME = "mp3.tagger.gui.config";
    private static final String CONFIG_KEY_DEFAULT_FOLDER = "default.folder";
    
    private static IConfiguration config = ConfigurationFactory.getBufferedConfiguration(CONFIG_NAME);
    
    public static File getDefaultFolder()
    {
	String path = config.getString(CONFIG_KEY_DEFAULT_FOLDER, "");
	if(!path.isEmpty())
	{
	    return new File(path);
	}
	else
	{
	    return File.listRoots()[0];
	}
    }
}
