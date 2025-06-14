/**
 * 🚀 ULTRA DASHBOARD JAVASCRIPT - NIVEL ENTERPRISE
 * Sistema avanzado de interacciones y animaciones
 */

window.UltraDashboard = {
    // Estado global
    state: {
        isActive: true,
        lastUpdate: Date.now(),
        connectionQuality: 'excellent',
        performanceScore: 98,
        animations: true,
        theme: 'dark'
    },
    
    // Inicialización ultra
    init() {
        console.log('🚀 Inicializando Ultra Dashboard...');
        
        this.setupParticleBackground();
        this.setupAdvancedInteractions();
        this.setupKeyboardShortcuts();
        this.setupPerformanceMonitoring();
        this.setupThemeSystem();
        this.startHeartbeat();
        
        console.log('✅ Ultra Dashboard inicializado correctamente');
    },
    
    // Sistema de partículas de fondo
    setupParticleBackground() {
        const canvas = document.createElement('canvas');
        canvas.style.position = 'fixed';
        canvas.style.top = '0';
        canvas.style.left = '0';
        canvas.style.width = '100%';
        canvas.style.height = '100%';
        canvas.style.pointerEvents = 'none';
        canvas.style.zIndex = '-1';
        canvas.style.opacity = '0.1';
        
        document.body.appendChild(canvas);
        
        const ctx = canvas.getContext('2d');
        const particles = [];
        
        // Crear partículas
        for (let i = 0; i < 50; i++) {
            particles.push({
                x: Math.random() * window.innerWidth,
                y: Math.random() * window.innerHeight,
                vx: (Math.random() - 0.5) * 0.5,
                vy: (Math.random() - 0.5) * 0.5,
                size: Math.random() * 2 + 1,
                opacity: Math.random() * 0.5 + 0.2
            });
        }
        
        // Animar partículas
        const animateParticles = () => {
            canvas.width = window.innerWidth;
            canvas.height = window.innerHeight;
            
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            
            particles.forEach(particle => {
                // Actualizar posición
                particle.x += particle.vx;
                particle.y += particle.vy;
                
                // Rebotar en bordes
                if (particle.x < 0 || particle.x > canvas.width) particle.vx *= -1;
                if (particle.y < 0 || particle.y > canvas.height) particle.vy *= -1;
                
                // Dibujar partícula
                ctx.beginPath();
                ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2);
                ctx.fillStyle = `rgba(79, 70, 229, ${particle.opacity})`;
                ctx.fill();
                
                // Conectar partículas cercanas
                particles.forEach(other => {
                    const distance = Math.hypot(particle.x - other.x, particle.y - other.y);
                    if (distance < 100) {
                        ctx.beginPath();
                        ctx.moveTo(particle.x, particle.y);
                        ctx.lineTo(other.x, other.y);
                        ctx.strokeStyle = `rgba(79, 70, 229, ${0.1 - distance / 1000})`;
                        ctx.stroke();
                    }
                });
            });
            
            requestAnimationFrame(animateParticles);
        };
        
        animateParticles();
    },
    
    // Interacciones avanzadas
    setupAdvancedInteractions() {
        // Efecto de seguimiento del cursor
        document.addEventListener('mousemove', (e) => {
            this.createCursorTrail(e.clientX, e.clientY);
        });
        
        // Efectos hover avanzados para tarjetas
        document.querySelectorAll('.ultra-progress-card').forEach(card => {
            card.addEventListener('mouseenter', () => {
                this.animateCardEnter(card);
            });
            
            card.addEventListener('mouseleave', () => {
                this.animateCardLeave(card);
            });
        });
        
        // Efectos de click con ondas
        document.addEventListener('click', (e) => {
            this.createRippleEffect(e.target, e.clientX, e.clientY);
        });
    },
    
    // Atajos de teclado ultra
    setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            // Ctrl/Cmd + D = Toggle Dashboard
            if ((e.ctrlKey || e.metaKey) && e.key === 'd') {
                e.preventDefault();
                this.toggleDashboard();
            }
            
            // Ctrl/Cmd + F = Fullscreen
            if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
                e.preventDefault();
                this.toggleFullscreen();
            }
            
            // Ctrl/Cmd + R = Refresh Data
            if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
                e.preventDefault();
                this.refreshDashboard();
            }
            
            // Escape = Dismiss Alerts
            if (e.key === 'Escape') {
                this.dismissAlerts();
            }
        });
    },
    
    // Monitoreo de rendimiento
    setupPerformanceMonitoring() {
        const observer = new PerformanceObserver((list) => {
            const entries = list.getEntries();
            entries.forEach(entry => {
                if (entry.entryType === 'measure') {
                    console.log(`📊 Performance: ${entry.name} took ${entry.duration}ms`);
                }
            });
        });
        
        observer.observe({ entryTypes: ['measure'] });
        
        // Monitorear FPS
        let fps = 0;
        let lastTime = performance.now();
        
        const countFPS = (currentTime) => {
            fps++;
            if (currentTime >= lastTime + 1000) {
                this.state.performanceScore = Math.min(100, fps);
                this.updatePerformanceIndicator();
                fps = 0;
                lastTime = currentTime;
            }
            requestAnimationFrame(countFPS);
        };
        
        requestAnimationFrame(countFPS);
    },
    
    // Sistema de temas
    setupThemeSystem() {
        const themes = {
            dark: {
                '--ultra-primary': '#4F46E5',
                '--ultra-secondary': '#10B981',
                '--ultra-dark': '#0f1419'
            },
            blue: {
                '--ultra-primary': '#3B82F6',
                '--ultra-secondary': '#06B6D4',
                '--ultra-dark': '#0c1929'
            },
            purple: {
                '--ultra-primary': '#8B5CF6',
                '--ultra-secondary': '#A855F7',
                '--ultra-dark': '#1a0933'
            }
        };
        
        window.UltraDashboard.changeTheme = (themeName) => {
            const theme = themes[themeName];
            if (theme) {
                Object.entries(theme).forEach(([property, value]) => {
                    document.documentElement.style.setProperty(property, value);
                });
                this.state.theme = themeName;
                this.showNotification(`🎨 Tema cambiado a ${themeName}`, 'success');
            }
        };
    },
    
    // Sistema de latidos (heartbeat)
    startHeartbeat() {
        setInterval(() => {
            this.state.lastUpdate = Date.now();
            this.pulseConnectionIndicator();
        }, 5000);
    },
    
    // Utilidades de animación
    createCursorTrail(x, y) {
        const trail = document.createElement('div');
        trail.style.position = 'fixed';
        trail.style.left = x + 'px';
        trail.style.top = y + 'px';
        trail.style.width = '6px';
        trail.style.height = '6px';
        trail.style.background = 'rgba(79, 70, 229, 0.6)';
        trail.style.borderRadius = '50%';
        trail.style.pointerEvents = 'none';
        trail.style.zIndex = '9999';
        trail.style.transition = 'all 0.5s ease-out';
        
        document.body.appendChild(trail);
        
        setTimeout(() => {
            trail.style.opacity = '0';
            trail.style.transform = 'scale(2)';
        }, 10);
        
        setTimeout(() => {
            document.body.removeChild(trail);
        }, 500);
    },
    
    createRippleEffect(element, x, y) {
        const rect = element.getBoundingClientRect();
        const ripple = document.createElement('div');
        
        ripple.style.position = 'absolute';
        ripple.style.left = (x - rect.left) + 'px';
        ripple.style.top = (y - rect.top) + 'px';
        ripple.style.width = '0';
        ripple.style.height = '0';
        ripple.style.borderRadius = '50%';
        ripple.style.background = 'rgba(255, 255, 255, 0.3)';
        ripple.style.transform = 'translate(-50%, -50%)';
        ripple.style.pointerEvents = 'none';
        ripple.style.transition = 'all 0.6s ease-out';
        
        element.style.position = 'relative';
        element.style.overflow = 'hidden';
        element.appendChild(ripple);
        
        setTimeout(() => {
            ripple.style.width = '200px';
            ripple.style.height = '200px';
            ripple.style.opacity = '0';
        }, 10);
        
        setTimeout(() => {
            element.removeChild(ripple);
        }, 600);
    },
    
    animateCardEnter(card) {
        card.style.transform = 'translateY(-8px) scale(1.02)';
        card.style.boxShadow = '0 20px 60px rgba(0,0,0,0.3)';
        
        // Añadir efecto de brillo
        const shine = document.createElement('div');
        shine.style.position = 'absolute';
        shine.style.top = '0';
        shine.style.left = '-100%';
        shine.style.width = '100%';
        shine.style.height = '100%';
        shine.style.background = 'linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent)';
        shine.style.transition = 'left 0.5s';
        shine.style.pointerEvents = 'none';
        
        card.appendChild(shine);
        
        setTimeout(() => {
            shine.style.left = '100%';
        }, 10);
        
        setTimeout(() => {
            card.removeChild(shine);
        }, 500);
    },
    
    animateCardLeave(card) {
        card.style.transform = 'translateY(0) scale(1)';
        card.style.boxShadow = '0 8px 32px rgba(0,0,0,0.1)';
    },
    
    // Funciones de control
    toggleDashboard() {
        const dashboard = document.querySelector('.ultra-dashboard');
        dashboard.style.opacity = dashboard.style.opacity === '0.5' ? '1' : '0.5';
        this.showNotification('🎛️ Dashboard toggled', 'info');
    },
    
    toggleFullscreen() {
        if (!document.fullscreenElement) {
            document.documentElement.requestFullscreen();
            this.showNotification('🖥️ Modo pantalla completa activado', 'success');
        } else {
            document.exitFullscreen();
            this.showNotification('🖥️ Modo pantalla completa desactivado', 'info');
        }
    },
    
    refreshDashboard() {
        this.showNotification('🔄 Actualizando dashboard...', 'info');
        
        // Simular refresh con animación
        const dashboard = document.querySelector('.ultra-dashboard');
        dashboard.style.opacity = '0.7';
        dashboard.style.transform = 'scale(0.98)';
        
        setTimeout(() => {
            dashboard.style.opacity = '1';
            dashboard.style.transform = 'scale(1)';
            this.showNotification('✅ Dashboard actualizado', 'success');
        }, 500);
        
        // Disparar evento personalizado para que Vaadin recargue datos
        window.dispatchEvent(new CustomEvent('dashboard-refresh'));
    },
    
    dismissAlerts() {
        const alerts = document.querySelectorAll('.ultra-alert-banner');
        alerts.forEach(alert => {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-20px)';
            setTimeout(() => {
                alert.style.display = 'none';
            }, 300);
        });
        this.showNotification('🔕 Alertas dismissed', 'info');
    },
    
    // Actualizadores visuales
    updatePerformanceIndicator() {
        const indicator = document.querySelector('.performance-ring span');
        if (indicator) {
            indicator.textContent = this.state.performanceScore + '%';
            
            // Cambiar color según rendimiento
            const ring = document.querySelector('.performance-ring');
            if (ring) {
                if (this.state.performanceScore > 90) {
                    ring.style.background = 'conic-gradient(#10B981 0deg, #10B981 ' + 
                        (this.state.performanceScore * 3.6) + 'deg, rgba(255,255,255,0.1) ' + 
                        (this.state.performanceScore * 3.6) + 'deg)';
                } else if (this.state.performanceScore > 70) {
                    ring.style.background = 'conic-gradient(#F59E0B 0deg, #F59E0B ' + 
                        (this.state.performanceScore * 3.6) + 'deg, rgba(255,255,255,0.1) ' + 
                        (this.state.performanceScore * 3.6) + 'deg)';
                } else {
                    ring.style.background = 'conic-gradient(#EF4444 0deg, #EF4444 ' + 
                        (this.state.performanceScore * 3.6) + 'deg, rgba(255,255,255,0.1) ' + 
                        (this.state.performanceScore * 3.6) + 'deg)';
                }
            }
        }
    },
    
    pulseConnectionIndicator() {
        const indicator = document.querySelector('.realtime-status');
        if (indicator) {
            indicator.style.transform = 'scale(1.05)';
            setTimeout(() => {
                indicator.style.transform = 'scale(1)';
            }, 200);
        }
    },
    
    // Sistema de notificaciones ultra
    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `ultra-notification ultra-notification-${type}`;
        
        const colors = {
            success: '#10B981',
            error: '#EF4444',
            warning: '#F59E0B',
            info: '#4F46E5'
        };
        
        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };
        
        notification.innerHTML = `
            <div style="
                position: fixed;
                top: 20px;
                right: 20px;
                background: linear-gradient(135deg, ${colors[type]}, ${colors[type]}CC);
                color: white;
                padding: 1rem 1.5rem;
                border-radius: 12px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.2);
                backdrop-filter: blur(10px);
                border: 1px solid rgba(255,255,255,0.1);
                z-index: 10000;
                font-weight: 600;
                font-size: 0.875rem;
                max-width: 400px;
                transform: translateX(100%);
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            ">
                ${icons[type]} ${message}
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Animar entrada
        setTimeout(() => {
            notification.firstElementChild.style.transform = 'translateX(0)';
        }, 10);
        
        // Auto dismiss
        setTimeout(() => {
            notification.firstElementChild.style.transform = 'translateX(100%)';
            notification.firstElementChild.style.opacity = '0';
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        }, 3000);
    },
    
    // Efectos especiales
    createFloatingElements() {
        for (let i = 0; i < 5; i++) {
            const element = document.createElement('div');
            element.style.position = 'fixed';
            element.style.width = '4px';
            element.style.height = '4px';
            element.style.background = 'rgba(79, 70, 229, 0.6)';
            element.style.borderRadius = '50%';
            element.style.left = Math.random() * window.innerWidth + 'px';
            element.style.top = window.innerHeight + 'px';
            element.style.pointerEvents = 'none';
            element.style.zIndex = '1';
            
            document.body.appendChild(element);
            
            // Animar hacia arriba
            const animation = element.animate([
                { transform: 'translateY(0) scale(1)', opacity: '0.6' },
                { transform: `translateY(-${window.innerHeight + 100}px) scale(0)`, opacity: '0' }
            ], {
                duration: Math.random() * 3000 + 2000,
                easing: 'ease-out'
            });
            
            animation.onfinish = () => {
                document.body.removeChild(element);
            };
        }
    },
    
    // Modo de demostración
    startDemoMode() {
        console.log('🎬 Iniciando modo demostración...');
        
        let step = 0;
        const demoSteps = [
            () => this.showNotification('🚀 Bienvenido al Ultra Dashboard', 'success'),
            () => this.showNotification('📊 Métricas en tiempo real', 'info'),
            () => this.showNotification('⚡ Rendimiento optimizado', 'success'),
            () => this.showNotification('🎨 Interfaz ultra moderna', 'info'),
            () => this.changeTheme('blue'),
            () => this.changeTheme('purple'),
            () => this.changeTheme('dark'),
            () => this.showNotification('🎉 Demo completada', 'success')
        ];
        
        const runDemo = () => {
            if (step < demoSteps.length) {
                demoSteps[step]();
                step++;
                setTimeout(runDemo, 2000);
            }
        };
        
        runDemo();
    },
    
    // Análisis de rendimiento
    analyzePerformance() {
        const metrics = {
            loadTime: performance.now(),
            memory: performance.memory ? {
                used: Math.round(performance.memory.usedJSHeapSize / 1048576),
                total: Math.round(performance.memory.totalJSHeapSize / 1048576),
                limit: Math.round(performance.memory.jsHeapSizeLimit / 1048576)
            } : null,
            fps: this.state.performanceScore,
            connections: this.state.connectionQuality
        };
        
        console.table(metrics);
        return metrics;
    },
    
    // Comando de consola para desarrolladores
    debug() {
        console.log('🔧 Ultra Dashboard Debug Info:');
        console.log('State:', this.state);
        console.log('Performance:', this.analyzePerformance());
        console.log('Available commands: changeTheme(), startDemoMode(), refreshDashboard()');
    }
};

// Auto-inicialización cuando el DOM está listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        setTimeout(() => window.UltraDashboard.init(), 500);
    });
} else {
    setTimeout(() => window.UltraDashboard.init(), 500);
}

// Comandos globales para desarrolladores
window.ultraDemo = () => window.UltraDashboard.startDemoMode();
window.ultraDebug = () => window.UltraDashboard.debug();
window.ultraTheme = (theme) => window.UltraDashboard.changeTheme(theme);

// Eventos especiales
window.addEventListener('beforeunload', () => {
    console.log('🚀 Ultra Dashboard cerrándose...');
});

// Manejo de errores global
window.addEventListener('error', (e) => {
    console.error('❌ Ultra Dashboard Error:', e.error);
    window.UltraDashboard.showNotification('Error del sistema detectado', 'error');
});

console.log('🚀 Ultra Dashboard JS cargado - Comandos disponibles: ultraDemo(), ultraDebug(), ultraTheme()');