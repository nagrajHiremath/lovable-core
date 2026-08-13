import { cn } from "@/lib/utils";

interface LogoProps {
  className?: string;
  hideText?: boolean;
}

export function Logo({ className, hideText }: LogoProps) {
  return (
    <div className={cn("inline-flex items-center gap-3", className)}>
      <div className="flex h-12 w-12 items-center justify-center rounded-3xl">
        <svg viewBox="0 0 100 100" className="h-8 w-8" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="lovableLogoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#FF6A5C" />
              <stop offset="50%" stopColor="#E056A7" />
              <stop offset="100%" stopColor="#7C4DFF" />
            </linearGradient>
          </defs>
          <polygon points="0,0 0,100 100,100" fill="url(#lovableLogoGradient)" />
        </svg>
      </div>
      {!hideText && (
        <div className="flex flex-col leading-tight">
          <span className="text-lg font-semibold tracking-tight">Lovable</span>
          <span className="text-xs uppercase tracking-[0.25em] text-muted-foreground">Clone</span>
        </div>
      )}
    </div>
  );
}
