package com.zoomlee.zoo.ui.adapters;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.ui.view.ZMEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TagsSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Tag> mDataset = new ArrayList<>();

    private int lastFocussedPosition = -1;
    private Handler handler = new Handler();

    private OnTagModifiedListener listener;

    public void clearFocus() {
        lastFocussedPosition = -1;
    }

    public interface OnTagModifiedListener {
        void onFocusChanged(View v, boolean hasFocus, int position);

        void onDelete(Tag tag, int position);
    }

    public void setData(List<Tag> myDataset) {
        if (myDataset == null)
            return;
        mDataset = myDataset;
        Collections.sort(mDataset, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return rhs.getUpdateTime() - lhs.getUpdateTime();
            }
        });
        notifyDataSetChanged();
    }

    public void setListener(OnTagModifiedListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tag_setting, parent, false);

            return new VHItem(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VHItem itemHolder = (VHItem) holder;
        Tag tag = mDataset.get(position);
        itemHolder.bindTag(tag);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void removeAt(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
    }

    public void add(int position, Tag tag) {
        mDataset.add(position, tag);
        notifyItemInserted(position);
    }

    public List<Tag> getData() {
        return mDataset;
    }

    private class VHItem extends RecyclerView.ViewHolder {
        private ZMEditText editTagView;
        private TextView docCountTv;
        private View deleteTagBtn;
        private Tag tag;

        public VHItem(View itemView) {
            super(itemView);
            editTagView = (ZMEditText) itemView.findViewById(R.id.editTagView);
            docCountTv = (TextView) itemView.findViewById(R.id.docCountTv);
            deleteTagBtn = itemView.findViewById(R.id.deleteTagBtn);
            initListeners();
        }

        public void bindTag(Tag tag) {
            this.tag = tag;
            this.editTagView.setText(tag.getName());
            this.docCountTv.setText(String.valueOf(tag.getDocsCount()));
            if (lastFocussedPosition == getPosition())
                editTagView.requestFocus();
        }

        private void initListeners() {
            editTagView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(final View v, boolean hasFocus) {
                    if (hasFocus) {
                        lastFocussedPosition = VHItem.this.getPosition();
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if (lastFocussedPosition == -1 || lastFocussedPosition == VHItem.this.getPosition()) {
                                    lastFocussedPosition = VHItem.this.getPosition();
                                    editTagView.requestFocus();
                                }
                                if (listener != null)
                                    listener.onFocusChanged(v, true, getPosition());
                            }
                        }, 200);
                    }
                }
            });
            editTagView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    tag.setName(s.toString());
                    tag.setUpdateTime((int) (System.currentTimeMillis() / 1000L));
                }
            });

            deleteTagBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener == null) return;
                    listener.onDelete(tag, getPosition());
                }
            });
        }
    }
}