package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.parsing.XNode;

import java.util.List;

public interface NodeHandler {
  void handleNode(XNode nodeToHandle, List<SqlNode> targetContents);
}
