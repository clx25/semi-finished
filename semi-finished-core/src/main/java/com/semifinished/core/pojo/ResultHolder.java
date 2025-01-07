package com.semifinished.core.pojo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResultHolder {
    private Page page;

    private List<ObjectNode> records;
}
