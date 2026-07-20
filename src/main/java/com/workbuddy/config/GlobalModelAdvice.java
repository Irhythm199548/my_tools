package com.workbuddy.config;

import com.workbuddy.entity.User;
import com.workbuddy.service.ToolCatalog;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局视图模型增强：为所有 Thymeleaf 页面注入工具目录与当前账号信息，
 * 供 fragments.html 导航、index.html 快捷入口按「已启用工具」做过滤。
 * 仅对 @Controller（页面渲染）生效，不影响 @RestController 的 JSON 接口。
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final ToolCatalog toolCatalog;
    private final CurrentUser currentUser;

    public GlobalModelAdvice(ToolCatalog toolCatalog, CurrentUser currentUser) {
        this.toolCatalog = toolCatalog;
        this.currentUser = currentUser;
    }

    /** 全部工具元信息 */
    @ModelAttribute("allTools")
    public List<ToolCatalog.ToolMeta> allTools() {
        return toolCatalog.all();
    }

    /** 当前账号启用的工具 key 列表；未登录时返回全部 */
    @ModelAttribute("enabledTools")
    public List<String> enabledTools() {
        return currentUser.getUser()
                .map(u -> toolCatalog.resolveEnabled(u.getEnabledTools()))
                .orElse(toolCatalog.allKeys());
    }

    /** 当前账号启用的工具元信息（用于导航与快捷入口） */
    @ModelAttribute("enabledToolMetas")
    public List<ToolCatalog.ToolMeta> enabledToolMetas() {
        List<String> enabled = enabledTools();
        List<ToolCatalog.ToolMeta> result = new ArrayList<>();
        for (ToolCatalog.ToolMeta t : toolCatalog.all()) {
            if (enabled.contains(t.key)) {
                result.add(t);
            }
        }
        return result;
    }

    /** 当前登录用户（未登录为 null），用于导航展示头像/昵称 */
    @ModelAttribute("me")
    public User me() {
        return currentUser.getUser().orElse(null);
    }
}
