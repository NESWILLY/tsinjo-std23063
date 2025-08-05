package com.hei.tsinjo.service;

import com.hei.tsinjo.domain.model.Help;
import com.hei.tsinjo.repository.HelpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelpService {

    private final HelpRepository helpRepository;

    public List<Help> getAllHelps() {
        return helpRepository.findAllOrderByCreatedAtDesc();
    }
}
