output "alb_dns_name"           { value = aws_lb.main.dns_name }
output "alb_arn"                { value = aws_lb.main.arn }
output "backend_tg_arn"         { value = aws_lb_target_group.backend.arn }
output "frontend_tg_arn"        { value = aws_lb_target_group.frontend.arn }
output "http_listener_arn"      { value = aws_lb_listener.http.arn }
