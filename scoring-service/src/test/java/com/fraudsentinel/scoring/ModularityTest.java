package com.fraudsentinel.scoring;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Mỗi package con trực tiếp của com.fraudsentinel.scoring là một "module".
 * Test này fail khi:
 *  - các module phụ thuộc vòng tròn vào nhau (ví dụ alert → messaging → scoring → alert);
 *  - một module dùng class nằm trong package con (nội bộ) của module khác, ví dụ rule.impl.
 * Nhờ vậy ranh giới của modular monolith được build kiểm tra tự động, không chỉ nhờ review.
 */
class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(ScoringServiceApplication.class);

    @Test
    void modules_have_no_cycles_and_only_use_each_others_public_packages() {
        modules.verify();
    }

    @Test
    void print_module_structure() {
        // In ra cấu trúc module trong log test: tiện để xem ai phụ thuộc vào ai
        modules.forEach(System.out::println);
    }
}
