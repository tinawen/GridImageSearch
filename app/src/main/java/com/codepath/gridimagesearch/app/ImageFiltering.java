package com.codepath.gridimagesearch.app;

import java.io.Serializable;

/**
 * Created by tinawen on 6/13/14.
 */
public class ImageFiltering implements Serializable {
    String imageSize;
    String colorFilter;
    String imageType;
    String siteFilter;

    public String getImageSize() {
        return imageSize;
    }

    public String getColorFilter() {
        return colorFilter;
    }

    public String getImageType() {
        return imageType;
    }

    public String getSiteFilter() {
        return siteFilter;
    }

    public ImageFiltering(String imageSize, String colorFilter, String imageType, String siteFilter) {
        this.imageSize = imageSize;
        this.colorFilter = colorFilter;
        this.imageType = imageType;
        this.siteFilter = siteFilter;
    }
}
