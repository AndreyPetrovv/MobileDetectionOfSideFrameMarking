package org.pytorch.demo.objectdetection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CropAdapter extends ArrayAdapter<ImageCrop> {

    Context mContext;
    List<ImageCrop> mImageCrops;

    public CropAdapter(Context context, List<ImageCrop> imageCrops) {
        super(context, R.layout.list_item, imageCrops);
        this.mContext = context;
        this.mImageCrops = imageCrops;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        }

        final ImageCrop currImageCrop = getItem(position);
        final ImageView photo = convertView.findViewById(R.id.image_crop);
        final CheckBox checkBox = convertView.findViewById(R.id.checkbox_feed);
        final TextView classText = convertView.findViewById(R.id.classText);
        final TextView ocrText = convertView.findViewById(R.id.ocrText);

        photo.setImageBitmap(currImageCrop.image);
        //checkBox.setChecked(currImageCrop.isChoice);
        classText.setText(currImageCrop.cropClass);
        ocrText.setText(currImageCrop.ocrValue);

        return convertView;
    }
}
