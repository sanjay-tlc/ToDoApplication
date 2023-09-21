package com.example.todo.projectadapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.TypeFaceUtil;
import com.example.todo.dao.ProjectDao;
import com.example.todo.model.Project;

import java.util.Collections;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private final List<Project> projects;
    private final ProjectDao projectDao;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(final int position);
        void onRemoveButtonClick(int position);
        void onProjectOrderUpdateListener(final Project fromProject, final Project toProject);
    }

    public ProjectAdapter(final List<Project> projects, final ProjectDao projectDao) {
        this.projects = projects;
        this.projectDao = projectDao;
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProjectAdapter.ViewHolder holder, final int position) {
        final Project project = projects.get(position);
        final Typeface typeface = TypeFaceUtil.getSelectedTypeFace();
        final float fontSize = TypeFaceUtil.getSelectedFontSize();

        if (null != typeface) {
            holder.projectNameTextView.setTypeface(typeface);
        } else if (0 != fontSize) {
            holder.projectNameTextView.setTextSize(fontSize);
        }

        holder.projectNameTextView.setText(project.getName());
        holder.itemView.setOnClickListener(view -> {
            if (null != onItemClickListener) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearProjects() {
        projects.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addProjects(final List<Project> newProjects) {
        projects.addAll(newProjects);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }


    public void onItemMove(final int fromPosition, final int toPosition) {
        final Project fromProject = projects.get(fromPosition);
        final Project toProject = projects.get(toPosition);

        Collections.swap(projects, fromPosition, toPosition);
        fromProject.setOrder((long) (toPosition + 1));
        toProject.setOrder((long) (fromPosition + 1));
        notifyItemMoved(fromPosition, toPosition);
        onItemClickListener.onProjectOrderUpdateListener(fromProject, toProject);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final TextView projectNameTextView;
        final ImageButton removeButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(R.id.projectNameTextView);
            removeButton = itemView.findViewById(R.id.removeButton);

            removeButton.setOnClickListener(view -> {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION && null != onItemClickListener) {
                    onItemClickListener.onRemoveButtonClick(position);
                    final Project project = projects.get(position);

                    if (projects.contains(project)) {
                        projects.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            });
        }
    }
}