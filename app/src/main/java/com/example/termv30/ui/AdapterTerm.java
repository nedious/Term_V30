package com.example.termv30.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.termv30.R;
import com.example.termv30.entities.CourseEntity;
import com.example.termv30.entities.TermEntity;

import java.util.List;

public class AdapterTerm extends RecyclerView.Adapter<AdapterTerm.TermsViewHolder> {

    private final LayoutInflater mInflater;
    private final Context context;
    public List<TermEntity> mTerms;
    private List<CourseEntity> mCourses;


    public AdapterTerm(Context context) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    class TermsViewHolder extends RecyclerView.ViewHolder {
        private final TextView termsItemView;
        private final TextView coursesItemList;

        private TermsViewHolder(View itemView){
            super(itemView);
            termsItemView = itemView.findViewById(R.id.item_term_title_textView);
            coursesItemList = itemView.findViewById(R.id.item_term_course_list_textView);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick (View v) {
                    int position = getAdapterPosition();
                    final TermEntity currentTerm = mTerms.get(position);
                    Intent intent = new Intent(context, ActivityTermDetail.class);
                    intent.putExtra("termID", currentTerm.getTermID());
                    intent.putExtra("position", position);
                    context.startActivity(intent);
                }
            });
        }

    }

    @Override
    public TermsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_term_list, parent, false);
        return new TermsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TermsViewHolder holder, int position) {

        if(mTerms != null) {
            final TermEntity currentTerm = mTerms.get(position);
            holder.termsItemView.setText((currentTerm.getTermTitle()));

            String coursesPerTerm = "";
            for(CourseEntity course: mCourses) {
                if (course.getTermID() == currentTerm.getTermID())
                    coursesPerTerm = coursesPerTerm + course.getCourseTitle() + "\n";
            }
            if (coursesPerTerm != "")
                holder.coursesItemList.setText(coursesPerTerm);
            else
                holder.coursesItemList.setVisibility(View.GONE);

        } else {
            holder.termsItemView.setText("no text");
        }
    }

    @Override
    public int getItemCount() {
        if (mTerms != null)
            return mTerms.size();
        else return 0;
    }

    public void setTerms(List<TermEntity> terms) {
        mTerms = terms;
        notifyDataSetChanged();
    }

    public void setCourses(List<CourseEntity> courses) {
        mCourses = courses;
        notifyDataSetChanged();
    }

    public TermEntity getTermAt(int position) {
        return mTerms.get(position);
    }

}
