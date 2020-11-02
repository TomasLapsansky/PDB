package com.vut.fit.pdb2020.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class homeController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/admin")
    public String admin() { return "index"; }

    @RequestMapping("/admin/users")
    public String adminUsersIndex() { return "index"; }

    @RequestMapping("/admin/users/create")
    public String adminUsersCreate() { return "index"; }

    @RequestMapping("/admin/users/edit/{id}")
    public String adminUsersEdit(@PathVariable String id) { return "index"; }

    @RequestMapping("/admin/products")
    public String adminProductsIndex() { return "index"; }

    @RequestMapping("/admin/products/create")
    public String adminProductsCreate() { return "index"; }

    @RequestMapping("/admin/products/edit/{id}")
    public String adminProductsEdit(@PathVariable String id) { return "index"; }

    @RequestMapping("/admin/categories")
    public String adminCategoryIndex() { return "index"; }

    @RequestMapping("/admin/categories/create")
    public String adminCategoryCreate() { return "index"; }

    @RequestMapping("/admin/categories/edit/{id}")
    public String adminCategoryEdit(@PathVariable String id) { return "index"; }

    @RequestMapping("/admin/orders")
    public String adminOrdersIndex() { return "index"; }

    @RequestMapping("/admin/orders/{id}")
    public String adminOrdersDetail(@PathVariable String id) { return "index"; }

    @RequestMapping("/admin/taxes")
    public String adminTaxIndex() { return "index"; }

    @RequestMapping("/admin/taxes/create")
    public String adminTaxCreate() { return "index"; }

    @RequestMapping("/admin/taxes/edit/{id}")
    public String adminTaxDetail(@PathVariable String id) { return "index"; }

    @RequestMapping("/product/{id}")
    public String frontendProductDetail(@PathVariable String id) { return "index"; }

    @RequestMapping("/cart")
    public String frontendCart() { return "index"; }

    @RequestMapping("/checkout")
    public String frontendCheckout() { return "index"; }

    @RequestMapping("/order-detail/{id}")
    public String frontendCheckout(@PathVariable String id) { return "index"; }

    @RequestMapping("/category/{id}")
    public String frontendCategoryProducts(@PathVariable String id) { return "index"; }

    @RequestMapping("/login")
    public String frontendLogin() { return "index"; }

    @RequestMapping("/register")
    public String frontendRegister() { return "index"; }

    @RequestMapping("/my-account")
    public String frontendMyAccount() { return "index"; }

    @RequestMapping("/my-account/orders")
    public String frontendMyOrders() { return "index"; }

    @RequestMapping("/my-account/orders/detail/{id}")
    public String frontendMyOrdersDetail(@PathVariable String id) { return "index"; }

}
