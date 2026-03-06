package com.llui.idrink.Interfaces;
/**
 * CommentDialogListener is an interface for notifying listeners when a comment is submitted.
 * Implement this interface to handle the event of submitting a comment along with its index.
 */
public interface CommentDialogListener {
    void onCommentSubmitted(String comment, int index);

}
