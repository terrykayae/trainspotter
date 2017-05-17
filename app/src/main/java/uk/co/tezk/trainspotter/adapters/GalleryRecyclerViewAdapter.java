package uk.co.tezk.trainspotter.adapters;

import android.content.Context;
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

    public GalleryRecyclerViewAdapter(List<String> imagePaths, Context context, OnImageClickListener imageClickListener) {
        // The first item in the list should always be a "Add photo" button
        this.images = new ArrayList<>();
        this.imageClickListener = imageClickListener;
        GalleryDrawable addPhotoButton = new GalleryDrawable();
        addPhotoButton.setUrl(TAKE_PHOTO);
        addPhotoButton.setDrawable(context.getResources().getDrawable(R.drawable.camera));
        this.images.add(addPhotoButton);
        // TODO : Get drawables for the list of images
        this.context = context;
        Log.i("GRVA", "constructor called...");
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_list_item, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, int position) {
        holder.galleryImage.setImageDrawable(images.get(position).getDrawable());
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
    }

    // Called by fragments to add images to the list, when camera is used for example
    public void addImage(String filename) {
        GalleryDrawable galleryDrawable = new GalleryDrawable();
        galleryDrawable.setUrl("From camera");
        galleryDrawable.setDrawable(Drawable.createFromPath(filename));
        images.add(galleryDrawable);
        Log.i("GRVA", "Added image " + galleryDrawable);
    }

    // In order to pass on click messages, out holder fragment must implement this
    public interface OnImageClickListener {
        void onClick(String imageUrl) ;
    }
}
