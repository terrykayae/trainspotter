package uk.co.tezk.trainspotter.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.tezk.trainspotter.R;

import static uk.co.tezk.trainspotter.model.Constant.TAKE_PHOTO;

/**
 * Created by tezk on 17/05/17.
 */

public class GalleryRecyclerViewAdapter extends RecyclerView.Adapter<GalleryRecyclerViewAdapter.GalleryViewHolder> {

    private List<GalleryDrawable> images;
    private Context context;
    private OnImageClickListener imageClickListener;
    private int imageViewHeight = 120;

    public GalleryRecyclerViewAdapter(List<String> imagePaths, Context context, OnImageClickListener imageClickListener, boolean addTakePhoto) {
        // The first item in the list should always be a "Add photo" button
        this.images = new ArrayList<>();
        this.imageClickListener = imageClickListener;
        if (addTakePhoto) {
            GalleryDrawable addPhotoButton = new GalleryDrawable();
            // Add image to the images list
            addPhotoButton.setUrl(TAKE_PHOTO);
            //addPhotoButton.setDrawable(context.getResources().getDrawable(R.drawable.camera));
            addPhotoButton.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.camera));
            this.images.add(addPhotoButton);
        }
        // TODO : Get drawables for the list of images
        this.context = context;
        // Now add all the images from the list...
        if (imagePaths!=null) {
            for (String each : imagePaths) {
                addImageFromFile(each);
            }
        }
        Log.i("GRVA", "constructor called...");
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_list_item, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, int position) {
        //holder.galleryImage.setImageDrawable(images.get(position).getDrawable());
        holder.galleryImage.setImageBitmap(images.get(position).getBitmap());
        float scale = (float)images.get(position).getBitmap().getWidth()/(float)images.get(position).getBitmap().getHeight();
        int h = holder.galleryImage.getHeight();
        if (h !=0)
            imageViewHeight = h;
        int setWidth = (int)(scale * (float)imageViewHeight);

        holder.galleryImage.setMinimumWidth(setWidth);
        holder.galleryImage.setMaxWidth(setWidth);
        holder.url = images.get(position).getUrl();

        // Dynamically add extra padding to the right hand side of last item
        if (position == getItemCount() - 1) {
            int paddingLeft = holder.galleryImage.getPaddingLeft();
            holder.galleryImage.setPadding(paddingLeft, 0, paddingLeft, 0);
            holder.galleryImage.measure(0, 0);
        }

        holder.galleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageClickListener!=null)
                    imageClickListener.onClick(holder.url);

            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivGallery)
        ImageView galleryImage;
        String url;

        public GalleryViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    // Class to hold the drawables and the URLs together in the list
    public class GalleryDrawable {
        private Drawable drawable;
        private Bitmap bitmap;
        private String Url;

        public Drawable getDrawable() {
            return drawable;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        public String getUrl() {
            return Url;
        }

        public void setUrl(String url) {
            Url = url;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }

    // Called by fragments to add images to the list, when camera is used for example
    public void addImageFromFile(String filename) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, bmOptions);
        int imageHeight = bmOptions.outHeight;
        int imageWidth = bmOptions.outWidth;

        bmOptions.inSampleSize = imageHeight / imageViewHeight;
        bmOptions.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filename, bmOptions);

        GalleryDrawable galleryDrawable = new GalleryDrawable();
        galleryDrawable.setUrl(filename);
        //galleryDrawable.setDrawable(Drawable.createFromPath(filename));
        galleryDrawable.setBitmap(bitmap);
        images.add(galleryDrawable);
    }

    // In order to pass on click messages, out holder fragment must implement this
    public interface OnImageClickListener {
        void onClick(String imageUrl) ;
    }
}
