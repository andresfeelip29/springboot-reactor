package com.course.springboot.reactor.app.Entities;


import java.util.ArrayList;
import java.util.List;

public class CommentEntity {

    private List<String> comments;

    public CommentEntity() {
        this.comments = new ArrayList<>();
    }

    public void addComments(String comment) {
        this.comments.add(comment);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommentEntity{");
        sb.append("comments=").append(comments);
        sb.append('}');
        return sb.toString();
    }
}
