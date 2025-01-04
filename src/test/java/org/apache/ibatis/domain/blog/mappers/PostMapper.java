package org.apache.ibatis.domain.blog.mappers;

import org.apache.ibatis.domain.blog.Post;

import java.util.Collection;
import java.util.List;

public interface PostMapper {

  List<Post> selectOddPostsInKeysList(Collection<Integer> keys);
}
