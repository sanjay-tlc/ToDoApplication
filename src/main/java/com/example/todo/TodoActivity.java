package com.example.todo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todo.controller.TodoController;
import com.example.todo.model.Todo;
import com.example.todo.model.TodoList;
import com.example.todo.service.TodoService;

import java.util.List;

/**
 * <p>
 * This activity displays a list of todo items for a specific project
 * </p>
 *
 * @author sanjai
 * @version 1.0
 */
public class TodoActivity extends AppCompatActivity implements TodoService{

    private TodoController todoController;
    private TodoList todoList;
    private EditText editText;
    private Button addButton;
    private ImageView backButton;
    private ImageView search;
    private ImageView addList;
    private ImageView previous;
    private ImageView next;
    private TableLayout layout;
    private Spinner filterSpinner;
    private String selectedList;
    private Long projectId;
    private Long id = 0L;
    private List<Todo> todoItems;
    private int currentPage = 1;
    private TextView pageNumber;
    private int pageSize = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_childproject);

        initializeData();
        initializeViews();
        initializeListeners();
        loadTodoList(selectedList);
    }

    private void initializeData() {
        projectId = getIntent().getLongExtra(getString(R.string.project_id), 0L);
        selectedList = getIntent().getStringExtra(getString(R.string.project_name));
        todoList = new TodoList();
        todoItems = todoList.getAllList();

        if (selectedList != null) {
            final TextView textView = findViewById(R.id.textView);

            textView.setText(selectedList);
        }
    }

    private void initializeViews() {
        todoController = new TodoController(this, this);
        backButton = findViewById(R.id.backButton1);
        search = findViewById(R.id.search);
        addList = findViewById(R.id.addButton);
        addButton = findViewById(R.id.button);
        layout = findViewById(R.id.tableLayout);
        editText = findViewById(R.id.editText);
        filterSpinner = findViewById(R.id.filter);
        pageNumber = findViewById(R.id.pageCount);
        previous = findViewById(R.id.prev_page);
        next = findViewById(R.id.next_page);
    }

    private void initializeListeners() {
        backButton = findViewById(R.id.backButton1);
        addButton = findViewById(R.id.button);
        search = findViewById(R.id.search);
        addList = findViewById(R.id.addButton);
        previous = findViewById(R.id.prev_page);
        next = findViewById(R.id.next_page);

        backButton.setOnClickListener(view -> onBackPressed());
        addButton.setOnClickListener(view -> todoController.onAddItem());
        search.setOnClickListener(view -> todoController.goToSearchActivity());
        addList.setOnClickListener(view -> todoController.onClickAddVisibility());
        next.setOnClickListener(view -> todoController.onClickNextPage());
        previous.setOnClickListener(view -> todoController.onclickPreviousPage());
        todoController.setupFilterSpinner();
    }

    /**
     * <p>
     * Add a new item to the todo list
     * </p>
     */
    public void onAddItem() {
        final String text = editText.getText().toString().trim();

        if (!text.isEmpty()) {
            final Todo todo = new Todo(text);

            todo.setParentId(projectId);
            todo.setId(++id);
            todo.setStatus("Not completed");
            todoList.add(todo);
            updateTableLayout();
            updatePageNumber();
            saveTodoList();
            editText.getText().clear();
        }
    }

    @Override
    public void setupFilterSpinner() {
        final ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.page_filter, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int i, final long id) {
                pageSize = Integer.parseInt(parent.getItemAtPosition(i).toString());

                updateTableLayout();
                updatePageNumber();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void navigateToSearchActivity() {
        final Intent intent = new Intent(TodoActivity.this, SearchActivity.class);

        intent.putExtra(getString(R.string.search_view), selectedList);
        startActivity(intent);
    }

    @Override
    public void toggleAddListVisibility() {
        final int visibility = editText.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        editText.setVisibility(visibility);
        addButton.setVisibility(visibility);
        filterSpinner.setVisibility(visibility);
    }

    @Override
    public void navigateToNextPage() {
        if ((currentPage * pageSize) < todoItems.size()) {
            currentPage++;
            updateTableLayout();
            updatePageNumber();
        }
    }

    @Override
    public void navigateToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTableLayout();
            updatePageNumber();
        }
    }

    @SuppressLint("DefaultLocale")
    private void updatePageNumber() {
        final int totalPage = (int) Math.ceil((double) todoItems.size() / pageSize);
        pageNumber.setText(String.format("%d / %d", currentPage, totalPage));
    }

    private void updateTableLayout() {
        layout.removeAllViews();
        final int startIndex = (currentPage - 1) * pageSize;
        final int endIndex = Math.min(startIndex + pageSize, todoItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            createTableRow(todoItems.get(i));
        }
    }

    private void createTableRow(final Todo todo) {
        final TableRow tableRow = new TableRow(this);
        final CheckBox checkBox = new CheckBox(this);
        final TextView textView = new TextView(this);
        final ImageView closeIcon = new ImageView(this);

        tableRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
        ));

        checkBox.setChecked(getCheckedBoxState(todo.getLabel()));
        checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                textView.setTextColor(Color.RED);
                todo.setChecked();
            } else {
                textView.setTextColor(Color.BLACK);
                todo.setChecked();
            }
            saveCheckedBoxState(todo.getLabel(), isChecked);
        });

        tableRow.addView(checkBox);
        textView.setText(todo.getLabel());
        tableRow.addView(textView);
        closeIcon.setImageResource(R.drawable.close);
        closeIcon.setOnClickListener(view -> removeItem(tableRow, todo));
        tableRow.addView(closeIcon);
        layout.addView(tableRow);
    }

    private void removeItem(final TableRow row, final Todo todo) {
        todoList.remove(todo.getId());
        layout.removeView(row);
        final int totalPageCount = (int) Math.ceil((double) todoItems.size() / pageSize);

        if (currentPage > totalPageCount) {
            currentPage = totalPageCount;
        }
        updatePageNumber();
        saveTodoList();
    }

    private void saveCheckedBoxState(final String label, final boolean isChecked) {
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference), MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(label, isChecked);
        editor.apply();
    }

    private boolean getCheckedBoxState(final String label) {
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference), MODE_PRIVATE);

        return sharedPreferences.getBoolean(label, false);
    }

    private void loadTodoList(final String listName) {
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference), MODE_PRIVATE);
        final String savedTodoItems = sharedPreferences.getString(listName, getString(R.string.space));
        final String[] todoItems = savedTodoItems.split(getString(R.string.kama));

        for (final String todoItem : todoItems) {
            if (!todoItem.isEmpty()) {
                final Todo todo = new Todo(todoItem);

                todoList.add(todo);
            }
        }
        updateTableLayout();
    }

    private void saveTodoList() {
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference), MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final StringBuilder todoItems = new StringBuilder();

        for (final Todo todo : todoList.getAllList()) {
            todoItems.append(todo.getLabel()).append(getString(R.string.kama));
        }

        editor.putString(selectedList, todoItems.toString());
        editor.apply();
    }
}