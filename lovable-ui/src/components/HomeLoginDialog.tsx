import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Loader2, Lock, Mail, Github, Globe } from "lucide-react";
import { api, setAuthToken, setUserInfo } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Logo } from "@/components/Logo";
import { useToast } from "@/hooks/use-toast";

export function HomeLoginDialog() {
  const [open, setOpen] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!email || !password) {
      toast({
        title: "Missing credentials",
        description: "Please enter both email and password",
        variant: "destructive",
      });
      return;
    }

    setIsLoading(true);

    try {
      const response = await api.login({ username: email, password });
      setAuthToken(response.token);
      if (response.user) {
        setUserInfo(response.user);
      }
      toast({
        title: "Welcome back!",
        description: "Successfully logged in",
      });
      setOpen(false);
      navigate("/projects");
    } catch (error) {
      toast({
        title: "Login failed",
        description: error instanceof Error ? error.message : "Invalid credentials",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button className="rounded-full px-8 py-3 text-base">Sign in</Button>
      </DialogTrigger>

      <DialogContent className="max-w-lg p-6 sm:p-8">
        <DialogHeader className="text-center sm:text-left">
          <button
            type="button"
            onClick={() => {
              setOpen(false);
              navigate("/");
            }}
            className="mx-auto mb-6 flex items-center justify-center rounded-3xl border border-white/10 bg-[#111827]/90 p-4 shadow-[0_20px_50px_rgba(0,0,0,0.18)] sm:mx-0"
          >
            <Logo hideText />
          </button>
          <DialogTitle className="text-2xl sm:text-3xl">Welcome back</DialogTitle>
          <DialogDescription className="mt-2 text-sm text-muted-foreground">
            Securely sign in to continue to your workspace and launch AI-assisted collaboration.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-3 mt-6 mb-4 sm:grid-cols-2">
          <Button
            type="button"
            variant="outline"
            onClick={() => window.location.assign("/auth/google")}
            className="flex items-center justify-center gap-2 rounded-2xl border border-border/50 px-4 py-3 text-sm"
          >
            <Globe className="h-4 w-4" />
            Google
          </Button>
          <Button
            type="button"
            variant="outline"
            onClick={() => window.location.assign("/auth/github")}
            className="flex items-center justify-center gap-2 rounded-2xl border border-border/50 px-4 py-3 text-sm"
          >
            <Github className="h-4 w-4" />
            GitHub
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="space-y-2">
            <Label htmlFor="home-email" className="text-sm font-medium text-foreground">
              Email
            </Label>
            <div className="relative">
              <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                id="home-email"
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="pl-10 h-12 rounded-2xl border-border/50 bg-muted/50 text-sm focus:border-primary"
                disabled={isLoading}
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="home-password" className="text-sm font-medium text-foreground">
              Password
            </Label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                id="home-password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="pl-10 h-12 rounded-2xl border-border/50 bg-muted/50 text-sm focus:border-primary"
                disabled={isLoading}
              />
            </div>
          </div>

          <Button type="submit" disabled={isLoading} className="w-full rounded-full px-8 py-3 text-base">
            {isLoading ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Signing in...
              </>
            ) : (
              "Sign in"
            )}
          </Button>
        </form>

        <div className="mt-6 text-center text-sm text-muted-foreground">
          Or continue with Google or GitHub above.
        </div>
      </DialogContent>
    </Dialog>
  );
}
