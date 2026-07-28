package com.lovable.services.workspace_service.mapper;

import com.lovable.services.common_lib.dto.FileNode;
import com.lovable.services.workspace_service.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    FileNode toFileNode(ProjectFile projectFile);
    List<FileNode> toFileNodeList(List<ProjectFile> projectFileList);
}
