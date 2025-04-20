package com.freelance.controller;

import com.freelance.model.Project;
import java.util.List;

public class ProjectController {
    private Project projectModel;

    public ProjectController() {
        this.projectModel = new Project();
    }

    public boolean createProject(String title, String description, double budget, int clientId) {
        Project newProject = new Project(title, description, budget, clientId);
        return newProject.save();
    }

    public List<Project> getAllProjects() {
        return projectModel.findAll();
    }

    public List<Project> getClientProjects(int clientId) {
        return projectModel.findByClientId(clientId);
    }
} 