package com.zoomlee.zoo.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.ui.view.AttachmentView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 2/23/15
 */
public class FilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<File> mDataset = new ArrayList<>();
    private OnActionClicked listener;

    public interface OnActionClicked {
        void onDeleteItem(int position);

        void onItemClicked(int position);

        void onAddFile();
    }


    public FilesAdapter(List<File> myDataset) {
        if (myDataset != null)
            mDataset = myDataset;
    }

    public void setData(List<File> myDataset) {
        if (myDataset == null)
            return;
        mDataset = myDataset;
        notifyDataSetChanged();
    }

    public void setListener(OnActionClicked listener) {
        this.listener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_file, parent, false);

            return new VHItem(v);
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_files_header, parent, false);
            return new VHHeader(v);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {
            VHItem itemHolder = (VHItem) holder;
            File file = mDataset.get(position - 1);
            itemHolder.previewIv.setAttachmentFile(file);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + 1;
    }


    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public void removeAt(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position + 1);
        notifyItemRangeChanged(position, mDataset.size() + 1);
    }

    public void add(File file) {
        mDataset.add(0, file);
        notifyItemInserted(0);
    }

    public List<File> getFiles() {
        return mDataset;
    }

    private class VHHeader extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Button button;

        public VHHeader(View itemView) {
            super(itemView);
            button = (Button) itemView.findViewById(R.id.addFile);
            button.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null)
                listener.onAddFile();
        }
    }

    private class VHItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        public AttachmentView previewIv;

        public VHItem(View v) {
            super(v);
            previewIv = (AttachmentView) v.findViewById(R.id.preview_iv);
            v.findViewById(R.id.deleteBtn).setOnClickListener(this);
            previewIv.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getPosition() - 1;
            if (listener != null)
                switch (v.getId()) {
                    case R.id.deleteBtn:
                        listener.onDeleteItem(position);
                        break;
                    case R.id.preview_iv:
                        listener.onItemClicked(position);
                        break;
                }
        }
    }

}