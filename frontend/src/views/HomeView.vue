<template>
    <n-space vertical size="large" style="padding: 24px; max-width: 800px; margin: 0 auto;">
        <n-space vertical align="center" justify="center">
            <n-h1 style="color: #2c3e50;">Welcome to Carpooling</n-h1>
            <n-text style="color: #666; font-size: 18px;">
                Find rides, share journeys, and travel together with our carpooling platform.
            </n-text>
        </n-space>

        <n-space justify="center" style="margin-bottom: 48px;">
            <n-card style="flex: 1; max-width: 300px;" title="🚗 Find a Ride" embedded>
                <n-space vertical>
                    <n-text style="color: #666;">
                        Search for available rides in your area and book your seat.
                    </n-text>
                    <router-link to="/search" style="text-decoration: none;">
                        <n-button type="primary" block>Search Rides</n-button>
                    </router-link>
                </n-space>
            </n-card>

            <n-card style="flex: 1; max-width: 300px;" title="💰 Make Payments" embedded>
                <n-space vertical>
                    <n-text style="color: #666;">
                        Secure payment processing for your carpooling transactions.
                    </n-text>
                    <router-link v-if="authStore.isAuthenticated" to="/payments/new" style="text-decoration: none;">
                        <n-button type="success" block>New Payment</n-button>
                    </router-link>
                    <router-link v-else to="/login" style="text-decoration: none;">
                        <n-button type="success" block>Login to Pay</n-button>
                    </router-link>
                </n-space>
            </n-card>
        </n-space>

        <n-card v-if="!authStore.isAuthenticated" style="background: #e3f2fd;" title="Join Our Community" embedded>
            <n-space vertical>
                <n-text style="color: #666;">
                    Create an account to start carpooling and connect with drivers and passengers.
                </n-text>
                <n-space justify="center">
                    <router-link to="/register" style="text-decoration: none;">
                        <n-button type="primary">Register Now</n-button>
                    </router-link>
                    <router-link to="/login" style="text-decoration: none;">
                        <n-button>Sign In</n-button>
                    </router-link>
                </n-space>
            </n-space>
        </n-card>

        <n-card v-else style="background: #e8f5e8;" title="Welcome back!" embedded>
            <n-space vertical>
                <n-text style="color: #666;">
                    Ready to find your next ride or manage your payments?
                </n-text>
                <n-space justify="center">
                    <router-link to="/search" style="text-decoration: none;">
                        <n-button type="primary">Find Rides</n-button>
                    </router-link>
                    <router-link to="/payments/new" style="text-decoration: none;">
                        <n-button type="success">Make Payment</n-button>
                    </router-link>
                    <router-link to="/profile" style="text-decoration: none;">
                        <n-button>My Profile</n-button>
                    </router-link>
                </n-space>
            </n-space>
        </n-card>
    </n-space>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>