package com.example.todo.service;

public interface TodoService {

    void onAddTodoItem();

    void setupFilterSpinner();

    void navigateToSearchActivity();

    void toggleAddListVisibility();

    void navigateToNextPage();

    void navigateToPreviousPage();
}
