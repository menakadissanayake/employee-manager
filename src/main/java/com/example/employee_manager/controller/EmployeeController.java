
package com.example.employee_manager.controller;

import com.example.employee_manager.model.Employee;
import com.example.employee_manager.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public String listEmployees(Model model) {
        model.addAttribute(
                "employees",
                employeeService.getAllEmployees());
        return "employees";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "edit-employee";
    }

    @PostMapping
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "edit-employee";
        }

        if (employeeService.getAllEmployees().stream()
                .anyMatch(e -> e.getEmail().equalsIgnoreCase(employee.getEmail()))) {
            result.rejectValue(
                    "email",
                    "duplicate",
                    "This email is already registered");
            return "edit-employee";
        }

        employeeService.saveEmployee(employee);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Employee saved successfully");

        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + id));

        model.addAttribute("employee", employee);
        return "edit-employee";
    }

    @PostMapping("/update/{id}")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "edit-employee";
        }

        Employee existing = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + id));

        boolean duplicateEmail = employeeService.getAllEmployees().stream()
                .anyMatch(e -> !e.getId().equals(id)
                        && e.getEmail().equalsIgnoreCase(employee.getEmail()));

        if (duplicateEmail) {
            result.rejectValue(
                    "email", "duplicate", "This email is already registered");
            return "edit-employee";
        }

        employee.setId(existing.getId());
        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Employee updated successfully");

        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        employeeService.deleteEmployee(id);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Employee deleted successfully");

        return "redirect:/employees";
    }
}
