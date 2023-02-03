package com.course.springboot.reactor.app.Entities;

public class UserCommentEntity {

    private UserEntity user;

    private CommentEntity comment;

    public UserCommentEntity(UserEntity user, CommentEntity comment) {
        this.user = user;
        this.comment = comment;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserCommentEntity{");
        sb.append("user=").append(user);
        sb.append(", comment=").append(comment);
        sb.append('}');
        return sb.toString();
    }
}
