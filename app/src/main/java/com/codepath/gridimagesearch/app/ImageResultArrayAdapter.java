package com.codepath.gridimagesearch.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.loopj.android.image.SmartImageView;

import java.util.List;
/**
 * Created by tinawen on 6/13/14.
 */
public class ImageResultArrayAdapter extends ArrayAdapter<ImageResult> {
    // View lookup cache
    private static class ViewHolder {
        String url;
        SmartImageView imageView;
    }

    public ImageResultArrayAdapter(Context context, List<ImageResult> images) {
        super(context, R.layout.item_image_result, images);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageResult imageInfo = this.getItem(position);
        ViewHolder viewHolder;
        String newUrl = imageInfo.getThumbUrl();
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_image_result, parent, false);
            convertView.setTag(viewHolder);
            viewHolder.url = newUrl;
            viewHolder.imageView = (SmartImageView) convertView.findViewById(R.id.image);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            // only erase image bitmap if we want to display a different image
            if (!viewHolder.url.equals(newUrl)) {
                viewHolder.imageView.setImageBitmap(null);
                viewHolder.url = newUrl;
            }
        }

        viewHolder.imageView.setImageUrl(newUrl);
        return convertView;
    }
}
