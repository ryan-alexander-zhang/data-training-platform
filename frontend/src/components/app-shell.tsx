"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Database, Rocket, Settings } from "lucide-react";

import { cn } from "@/lib/utils";

const navItems = [
  { href: "/", label: "概览", icon: Database },
  { href: "/datasets", label: "数据集", icon: Database },
  { href: "/training", label: "训练事件", icon: Rocket },
  { href: "/settings", label: "租户设置", icon: Settings }
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <div className="min-h-screen bg-muted/40">
      <div className="flex min-h-screen">
        <aside className="w-64 border-r border-border bg-card px-4 py-6">
          <div className="mb-8">
            <p className="text-sm text-muted-foreground">Data Training Platform</p>
            <h1 className="text-xl font-semibold">数据训练控制台</h1>
          </div>
          <nav className="space-y-1">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 rounded-md px-3 py-2 text-sm text-muted-foreground transition hover:bg-muted",
                  pathname === item.href && "bg-muted text-foreground"
                )}
              >
                <item.icon className="h-4 w-4" />
                {item.label}
              </Link>
            ))}
          </nav>
        </aside>
        <main className="flex-1 p-8">
          <div className="mx-auto max-w-5xl space-y-8">{children}</div>
        </main>
      </div>
    </div>
  );
}
