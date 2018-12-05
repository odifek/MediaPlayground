package com.sprinthub.example.mediaplayground.utils;

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 *
 * For more information, see:
 * https://medium.com/google-developers/livedata-with-events-ac2622673150
 */
public class Event<T> {

    private T content;

    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    public boolean isBeenHandled() {
        return hasBeenHandled;
    }

    public T peekContent() {
        return content;
    }
}
