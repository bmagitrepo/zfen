package com.zoomlee.zoo.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.ui.view.DocumentIconView;
import com.zoomlee.zoo.ui.view.ZMTextView;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.UiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 4/28/15
 */
public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.VHItem> {

    private final List<Document> mDataset = new ArrayList<>();
    private HashSet<Integer> selectedDocIds = new HashSet<>();
    private List<Person> persons;
    private HashMap<Integer, Integer> id2position;
    private boolean isShowPerson = false;
    private OnClickDocumentListener listener;

    public interface OnClickDocumentListener{
        void onClick(int position, boolean newSelected);
    }

    public void setListener(OnClickDocumentListener listener) {
        this.listener = listener;
    }

    public void setData(List<Document> myDataset) {
        if (myDataset == null) {
            return;
        }

        mDataset.clear();
        mDataset.addAll(myDataset);
        notifyDataSetChanged();
    }

    public void setPersons(List<Person> persons) {
        isShowPerson = true;
        this.persons = persons;
        id2position = new HashMap<>();
    }

    private Person getPerson(int personId) {
        if (personId == Person.ME_ID)
            return SharedPreferenceUtils.getUtils().getUserSettings();

        int position = -1;
        if (id2position.containsKey(personId))
            position = id2position.get(personId);
        else {
            int personsSize = persons.size();
            for (int i = 0; i < personsSize; ++i) {
                if (persons.get(i).getId() == personId) {
                    position = i;
                    break;
                }
            }
            id2position.put(personId, position);
        }

        return persons.get(position);
    }

    @Override
    public VHItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_document, parent, false);
        return new VHItem(v);
    }

    @Override
    public void onBindViewHolder(VHItem holder, int position) {
        Document doc = mDataset.get(position);
        boolean isSelected = selectedDocIds.contains(doc.getId());
        holder.name.setText(doc.getName());
        holder.iconView.setDocument(doc);
        holder.itemView.setSelected(isSelected);
        holder.check.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.avatar.setVisibility(isShowPerson ? View.VISIBLE : View.GONE);
        if (isShowPerson) {
            UiUtil.loadPersonIcon(getPerson(doc.getLocalPersonId()), holder.avatar, false);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void removeAt(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    public void add(Document document) {
        mDataset.add(document);
        notifyItemInserted(mDataset.size());
    }

    public List<Document> getData() {
        return mDataset;
    }

    public HashSet<Integer> getSelectedDocIds(){
        return selectedDocIds;
    }


    class VHItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ZMTextView name;
        public DocumentIconView iconView;
        public ImageView avatar;
        public ImageView check;

        public VHItem(View v) {
            super(v);
            name = (ZMTextView) v.findViewById(R.id.documentNameTv);
            iconView = (DocumentIconView) v.findViewById(R.id.document_icon);
            check = (ImageView) v.findViewById(R.id.checkIv);
            avatar = (ImageView) v.findViewById(R.id.avatarIv);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            boolean isSelect = !v.isSelected();
            int docId = mDataset.get(getPosition()).getId();
            if (isSelect)
                selectedDocIds.add(docId);
            else
                selectedDocIds.remove(docId);
            notifyItemChanged(getPosition());
            if(listener != null)
                listener.onClick(getPosition(), isSelect);
        }
    }

}