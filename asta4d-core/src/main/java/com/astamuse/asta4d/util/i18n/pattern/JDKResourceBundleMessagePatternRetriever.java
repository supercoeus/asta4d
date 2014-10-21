package com.astamuse.asta4d.util.i18n.pattern;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.astamuse.asta4d.Configuration;
import com.astamuse.asta4d.Context;

/**
 * We allow row splitted messages. For a given key, if there is no corresponding message, we will try to search keys as "key#1", "key#2"...
 * then combine them as a single message.
 * 
 * @author e-ryu
 * 
 */
public class JDKResourceBundleMessagePatternRetriever implements MessagePatternRetriever {

    private ResourceBundleFactory resourceBundleFactory = new CharsetResourceBundleFactory();

    private List<String> resourceNames = new LinkedList<>();

    public ResourceBundleFactory getResourceBundleFactory() {
        return resourceBundleFactory;
    }

    public void setResourceBundleFactory(ResourceBundleFactory resourceBundleFactory) {
        this.resourceBundleFactory = resourceBundleFactory;
    }

    public void setResourceNames(List<String> resourceNames) {
        this.resourceNames = new LinkedList<>(resourceNames);
    }

    @Override
    public String retrieve(Locale locale, String key) {
        String pattern = null;
        for (String resourceName : resourceNames) {
            try {
                ResourceBundle resourceBundle = getResourceBundle(resourceName, locale);
                pattern = retrieveResourceFromBundle(resourceBundle, key);
            } catch (MissingResourceException e) {
                //
            }
        }
        return pattern;
    }

    protected ResourceBundle getResourceBundle(String resourceName, Locale locale) {
        Configuration config = Configuration.getConfiguration();

        if (!config.isCacheEnable()) {
            ResourceBundle.clearCache();
        }

        if (locale == null) {
            locale = Context.getCurrentThreadContext().getCurrentLocale();
            if (locale == null) {
                locale = Locale.getDefault();
            }
        }
        return resourceBundleFactory.retrieveResourceBundle(resourceName, locale);
    }

    /**
     * In this method, we allow row splitted message. For a given key, if there is no corresponding message, we will try to search keys as
     * "key#1", "key#2"... then combine them as a single message.
     * 
     * @param bundle
     * @param key
     * @return
     */
    protected String retrieveResourceFromBundle(ResourceBundle bundle, String key) {
        String res = null;
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            // check if there is a splitted message
            try {
                res = bundle.getString(key + "#1");
            } catch (MissingResourceException ex) {
                //
                return null;
            }

            StringBuilder sb = new StringBuilder(res.length() * 3);
            sb.append(res);
            try {
                for (int row = 2;/* loop for ever */; row++) {
                    sb.append(bundle.getString(key + "#" + row));
                }
            } catch (MissingResourceException ex) {
                return sb.toString();
            }

        }
    }
}
