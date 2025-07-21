package com.example.groupassignment_group8;

import android.graphics.Paint;
import android.util.Log; // Import Log class
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskListAdapter extends ListAdapter<ListItem, RecyclerView.ViewHolder> {

    private OnItemClickListener clickListener;
    private OnTaskCheckedChangeListener checkedChangeListener;

    public TaskListAdapter() {
        super(DIFF_CALLBACK);
        setHasStableIds(true);
    }

    private static final DiffUtil.ItemCallback<ListItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<ListItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            if (oldItem.getType() != newItem.getType()) return false;
            if (oldItem instanceof TaskItem) {
                return ((TaskItem) oldItem).getTask().getFirestoreId().equals(((TaskItem) newItem).getTask().getFirestoreId());
            } else if (oldItem instanceof DateHeaderItem) {
                return ((DateHeaderItem) oldItem).getDate().equals(((DateHeaderItem) newItem).getDate());
            } else { // PriorityHeaderItem
                return ((PriorityHeaderItem) oldItem).getPriority().equals(((PriorityHeaderItem) newItem).getPriority());
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            if (oldItem instanceof TaskItem) {
                Task oldTask = ((TaskItem) oldItem).getTask();
                Task newTask = ((TaskItem) newItem).getTask();
                return oldTask.getTitle().equals(newTask.getTitle()) &&
                        oldTask.getDescription().equals(newTask.getDescription()) &&
                        oldTask.isCompleted() == newTask.isCompleted() && // Crucial check for completion status
                        oldTask.getPriority() == newTask.getPriority() &&
                        oldTask.getReminderTime() == newTask.getReminderTime();
            } else if (oldItem instanceof DateHeaderItem){
                return ((DateHeaderItem) oldItem).getDate().equals(((DateHeaderItem) newItem).getDate());
            } else { // PriorityHeaderItem
                return ((PriorityHeaderItem) oldItem).getPriority().equals(((PriorityHeaderItem) newItem).getPriority());
            }
        }
    };

    @Override
    public long getItemId(int position) {
        ListItem item = getItem(position);
        if (item.getType() == ListItem.TYPE_TASK) {
            return item.hashCode();
        } else if (item.getType() == ListItem.TYPE_HEADER) {
            return ((DateHeaderItem) item).getDate().hashCode();
        } else { // TYPE_PRIORITY_HEADER
            return ((PriorityHeaderItem) item).getPriority().hashCode();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ListItem.TYPE_HEADER || viewType == ListItem.TYPE_PRIORITY_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == ListItem.TYPE_HEADER) {
            DateHeaderItem headerItem = (DateHeaderItem) getItem(position);
            ((DateHeaderViewHolder) holder).bind(headerItem.getDate());
        } else if (viewType == ListItem.TYPE_PRIORITY_HEADER) {
            PriorityHeaderItem headerItem = (PriorityHeaderItem) getItem(position);
            ((DateHeaderViewHolder) holder).bind(headerItem.getPriority());
        } else {
            TaskItem taskItem = (TaskItem) getItem(position);
            ((TaskViewHolder) holder).bind(taskItem.getTask());
        }
    }

    public Task getTaskAt(int position) {
        ListItem item = getItem(position);
        if (item instanceof TaskItem) {
            return ((TaskItem) item).getTask();
        }
        return null;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private CheckBox completedCheckBox;
        private View priorityIndicator;
        private TextView reminderTextView;
        private ImageView checkMarkIcon;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            completedCheckBox = itemView.findViewById(R.id.completedCheckBox);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            reminderTextView = itemView.findViewById(R.id.reminderTextView);
            checkMarkIcon = itemView.findViewById(R.id.check_mark_icon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (clickListener != null && position != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(getTaskAt(position));
                }
            });

            completedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (checkedChangeListener != null && position != RecyclerView.NO_POSITION && buttonView.isPressed()) {
                    checkedChangeListener.onTaskCheckedChanged(getTaskAt(position), isChecked);
                }
            });
        }

        public void bind(final Task task) {
            Log.d("TaskAdapter", "Binding task: " + task.getTitle() + " (Completed: " + task.isCompleted() + ")");
            titleTextView.setText(task.getTitle());
            descriptionTextView.setText(task.getDescription());
            completedCheckBox.setChecked(task.isCompleted());
            updateTitleStrikeThrough(task.isCompleted());

            if (task.isCompleted()) {
                checkMarkIcon.setVisibility(View.VISIBLE);
                Log.d("TaskAdapter", "Checkmark for " + task.getTitle() + " set to VISIBLE.");
            } else {
                checkMarkIcon.setVisibility(View.GONE);
                Log.d("TaskAdapter", "Checkmark for " + task.getTitle() + " set to GONE.");
            }

            switch (task.getPriority()) {
                case 3: // High
                    priorityIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_high));
                    break;
                case 2: // Medium
                    priorityIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_medium));
                    break;
                default: // Low
                    priorityIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_low));
                    break;
            }

            if (task.getReminderTime() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                reminderTextView.setText(sdf.format(task.getReminderTime()));
                reminderTextView.setVisibility(View.VISIBLE);
            } else {
                reminderTextView.setVisibility(View.GONE);
            }
        }

        private void updateTitleStrikeThrough(boolean isCompleted) {
            if (isCompleted) {
                titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                titleTextView.setPaintFlags(titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView headerTitle;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.header_title);
        }

        public void bind(String title) {
            headerTitle.setText(title);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public interface OnTaskCheckedChangeListener {
        void onTaskCheckedChanged(Task task, boolean isChecked);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnTaskCheckedChangeListener(OnTaskCheckedChangeListener listener) {
        this.checkedChangeListener = listener;
    }
}
