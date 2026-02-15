package com.alem.GIA.controller;

import com.alem.GIA.DTO.DependentDto;
import com.alem.GIA.entity.Dependent;
import com.alem.GIA.model.DependentResponse;
import com.alem.GIA.repository.DependentRepository;
import com.alem.GIA.service.DependentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dependents")
@CrossOrigin(origins = "*")
public class DependentController {
    private final DependentService dependentService;

    public DependentController(DependentService dependentService) {
        this.dependentService = dependentService;
    }

    @GetMapping("/dependents")
    public List<Dependent> dependents(){
        return dependentService.getAlldependents();
    }
    @PostMapping("/addDependentToMember")
    public String addDependentToMember(@RequestBody DependentDto dto){
        return dependentService.addDependentToMember(dto);
    }
}
