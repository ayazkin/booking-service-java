package com.booking.javaproject.equipment.controller;

import com.booking.javaproject.equipment.model.Equipment;
import com.booking.javaproject.equipment.service.EquipmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/equipment")
public class AdminEquipmentController {

    private final EquipmentService equipmentService;

    public AdminEquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public String listEquipment(Model model) {
        model.addAttribute("equipment", equipmentService.findAll());
        return "admin/equipment/list";
    }

    @GetMapping("/new")
    public String newEquipmentForm() {
        return "admin/equipment/new";
    }

    @PostMapping
    public String createEquipment(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            Model model
    ) {
        if (name.isBlank()) {
            model.addAttribute("error", "Название оборудования обязательно");
            model.addAttribute("description", description);
            return "admin/equipment/new";
        }

        equipmentService.create(name, description);
        return "redirect:/admin/equipment";
    }

    @GetMapping("/{id}/edit")
    public String editEquipmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("equipment", equipmentService.findById(id));
        return "admin/equipment/edit";
    }

    @PostMapping("/{id}")
    public String updateEquipment(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean active,
            Model model
    ) {
        if (name.isBlank()) {
            Equipment equipment = equipmentService.findById(id);
            equipment.setDescription(description);
            equipment.setActive(active);
            model.addAttribute("equipment", equipment);
            model.addAttribute("error", "Название оборудования обязательно");
            return "admin/equipment/edit";
        }

        equipmentService.update(id, name, description, active);
        return "redirect:/admin/equipment";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateEquipment(@PathVariable Long id) {
        equipmentService.deactivate(id);
        return "redirect:/admin/equipment";
    }
}
