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
import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 2/23/15
 */
public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements NoDivider {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Tag> mDataset = new ArrayList<>();

    public enum SearchState {SEARCH, NOT_SEARCH, EMPTY_SEARCH}

    private SearchState searchState = SearchState.NOT_SEARCH;
    private OnItemSelectedListener listener;
    private boolean clean = false;

    private final Handler notifyHandler = new Handler();

    public interface OnItemSelectedListener {
        void onSelect(Tag tag);

        void onSearch(String s);

        void onSearchFocus();

        void onRemove(int position);
    }

    public void setData(List<Tag> myDataset) {
        if (myDataset == null) {
            return;
        }

        mDataset.clear();
        mDataset.addAll(myDataset);
        notifyHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemRangeChanged(1, getItemCount());
            }
        });
    }

    public void setListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tag, parent, false);

            return new VHItem(v);
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tag_header, parent, false);
            return new VHHeader(v);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {
            VHItem itemHolder = (VHItem) holder;
            Tag tag = mDataset.get(position - 1);
            itemHolder.deleteBtn.setVisibility(searchState == SearchState.SEARCH ? View.GONE : View.VISIBLE);
            itemHolder.nameTv.setText(tag.getName());
        } else if (holder instanceof VHHeader) {
            VHHeader itemHolder = (VHHeader) holder;
            if (clean) {
                itemHolder.addTagView.setText("");
                clean = false;
            }
            if (searchState == SearchState.SEARCH || searchState == SearchState.EMPTY_SEARCH) {
                itemHolder.addTagView.requestFocus();
            }
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

    @Override
    public boolean noDivider(int position) {
        return isPositionHeader(position) || getItemCount() == 1;
    }

    @Override
    public boolean noFooterLine() {
        return false;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public void removeAt(int position) {
        if (listener != null) {
            listener.onRemove(position);
        }

        mDataset.remove(position);
        notifyItemRemoved(position + 1);
        notifyItemRangeChanged(position + 1, mDataset.size() + 1);
    }

    public void add(Tag tag) {
        mDataset.add(tag);
        notifyItemInserted(mDataset.size() + 1);
    }

    public void setSearchMode(SearchState state) {
        searchState = state;

        notifyHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void cleanSearch() {
        clean = true;
        notifyItemChanged(0);
    }

    public List<Tag> getData() {
        return mDataset;
    }

    class VHItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameTv;
        public View deleteBtn;
        public View container;

        public VHItem(View v) {
            super(v);
            nameTv = (TextView) v.findViewById(R.id.nameTv);
            deleteBtn = v.findViewById(R.id.deleteBtn);
            deleteBtn.setOnClickListener(this);
            container = v.findViewById(R.id.container);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.deleteBtn:
                    removeAt(getPosition() - 1);
                    break;
                case R.id.container:
                    if (searchState == SearchState.SEARCH && listener != null)
                        listener.onSelect(mDataset.get(getPosition() - 1));
                    break;
            }
        }
    }

    private class VHHeader extends RecyclerView.ViewHolder {
        private View button;
        private ZMEditText addTagView;

        public VHHeader(View itemView) {
            super(itemView);
            addTagView = (ZMEditText) itemView.findViewById(R.id.addTagEt);
            button = itemView.findViewById(R.id.addNewTagBtn);
            initListeners();
        }

        private void initListeners() {
            addTagView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    button.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);

                    if (searchState != SearchState.NOT_SEARCH && listener != null)
                        listener.onSearch(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            addTagView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (searchState == SearchState.NOT_SEARCH && hasFocus && listener != null)
                        listener.onSearchFocus();
                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener == null) return;
                    Tag tag = new Tag();
                    tag.setName(addTagView.getText().toString());
                    listener.onSelect(tag);

                }
            });
        }
    }
}