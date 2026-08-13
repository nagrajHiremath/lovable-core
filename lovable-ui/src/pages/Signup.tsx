import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Loader2, Mail, User, Lock, Github, Globe } from "lucide-react";
import { api, setAuthToken, setUserInfo } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Logo } from "@/components/Logo";
import { useToast } from "@/hooks/use-toast";

export default function Signup() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name || !email || !password) {
      toast({
        title: "Missing details",
        description: "Please fill in all required fields",
        variant: "destructive",
      });
      return;
    }

    setIsLoading(true);

    try {
      const response = await api.signup({ name, username: email, password });
      setAuthToken(response.token);
      setUserInfo(response.user);
      toast({
        title: "Welcome!",
        description: "Your account is ready",
      });
      navigate("/projects");
    } catch (error) {
      toast({
        title: "Signup failed",
        description: error instanceof Error ? error.message : "Could not create account",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-background">
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute top-1/4 left-1/4 h-[520px] w-[520px] -translate-x-1/2 rounded-full bg-primary/10 blur-3xl" />
        <div className="absolute bottom-1/4 right-1/4 h-[420px] w-[420px] rounded-full bg-secondary/10 blur-3xl" />
      </div>

      <div className="relative w-full max-w-lg">
        <div className="bg-card border border-border/50 rounded-2xl p-8 shadow-2xl">
          <div className="text-center mb-8">
            <div className="mx-auto mb-5">
              <Logo hideText />
            </div>
            <h1 className="text-2xl font-semibold text-foreground mb-2">Get started</h1>
            <p className="text-muted-foreground text-sm">
              Sign up to save your workspace, generate AI-backed ideas, and manage your product flow.
            </p>
          </div>

          <div className="grid gap-3 mb-4 sm:grid-cols-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => window.location.assign("/auth/google")}
              className="flex items-center justify-center gap-2 rounded-2xl border border-border/50 px-4 py-3 text-sm"
            >
              <Globe className="h-4 w-4" />
              Sign up with Google
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={() => window.location.assign("/auth/github")}
              className="flex items-center justify-center gap-2 rounded-2xl border border-border/50 px-4 py-3 text-sm"
            >
              <Github className="h-4 w-4" />
              Sign up with GitHub
            </Button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-2">
              <Label htmlFor="name" className="text-sm font-medium text-foreground">
                Full Name
              </Label>
              <div className="relative">
                <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  id="name"
                  type="text"
                  placeholder="John Doe"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="pl-10 h-12 rounded-xl border-border/50 bg-muted/50 text-sm focus:border-primary"
                  disabled={isLoading}
                />
              </div>
            </div>


            <div className="space-y-2">
              <Label htmlFor="email" className="text-sm font-medium text-foreground">
                Email
              </Label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="pl-10 h-12 rounded-xl border-border/50 bg-muted/50 text-sm focus:border-primary"
                  disabled={isLoading}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-sm font-medium text-foreground">
                Password
              </Label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pl-10 h-12 rounded-xl border-border/50 bg-muted/50 text-sm focus:border-primary"
                  disabled={isLoading}
                />
              </div>
            </div>

            <Button
              type="submit"
              disabled={isLoading}
              className="w-full h-12 rounded-xl bg-primary text-primary-foreground font-medium text-sm hover:bg-primary/90"
            >
              {isLoading ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Getting started...
                </>
              ) : (
                "Get started"
              )}
            </Button>
          </form>

          <p className="text-center text-sm text-muted-foreground mt-6">
            Already have an account?{" "}
            <Link to="/login" className="text-primary hover:underline font-medium">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
