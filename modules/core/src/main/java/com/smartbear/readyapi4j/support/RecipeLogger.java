package com.smartbear.readyapi4j.support;

import com.smartbear.readyapi4j.TestRecipe;
import com.smartbear.readyapi4j.execution.RecipeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

/**
 * RecipeFilter that writes all recipes as files to the specified folder
 */
public class RecipeLogger implements RecipeFilter {

    private final static Logger LOG = LoggerFactory.getLogger(RecipeLogger.class);

    public static final String DEFAULT_PREFIX = "recipe";
    public static final String DEFAULT_EXTENSION = "json";

    private final String targetFolder;
    private final String prefix;
    private final String extension;

    public RecipeLogger(String targetFolder, String prefix, String extension) {
        this.targetFolder = targetFolder;
        this.prefix = prefix;
        this.extension = extension;
    }

    public RecipeLogger(String targetFolder) {
        this(targetFolder, DEFAULT_PREFIX, DEFAULT_EXTENSION);
    }

    @Override
    public void filterRecipe(TestRecipe testRecipe) {
        try {
            File directory = new File(targetFolder);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = File.createTempFile(prefix, "." + extension, directory);
            try ( FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(testRecipe.toString().getBytes());
            }
        } catch (Exception e) {
            LOG.error("Failed to write recipe to file", e);
        }
    }
}