import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Loader2, Lock, Mail, User } from "lucide-react";
import { api, setAuthToken, setUserInfo } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Logo } from "@/components/Logo";
import { useToast } from "@/hooks/use-toast";

type AuthMode = "signin" | "signup";

interface HomeAuthDialogProps {
  /** Custom trigger element. Omit for controlled usage (open/onOpenChange). */
  trigger?: React.ReactNode;
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
  initialMode?: AuthMode;
  /** A prompt typed on the landing page before the user was authenticated. */
  initialMessage?: string;
}

const DEFAULT_PROJECT_NAME = "New Project";

export function HomeAuthDialog({ trigger, open: controlledOpen, onOpenChange, initialMode = "signin", initialMessage }: HomeAuthDialogProps = {}) {
  const isControlled = controlledOpen !== undefined;
  const [internalOpen, setInternalOpen] = useState(false);
  const [mode, setMode] = useState<AuthMode>(initialMode);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { toast } = useToast();

  const open = isControlled ? controlledOpen : internalOpen;

  const setOpen = (nextOpen: boolean) => {
    if (onOpenChange) onOpenChange(nextOpen);
    if (!isControlled) setInternalOpen(nextOpen);
    if (!nextOpen) {
      clearForm();
      setMode(initialMode);
    }
  };

  const clearForm = () => {
    setName("");
    setEmail("");
    setPassword("");
  };

  // After a successful login/signup: if the user typed a prompt on the landing
  // page before authenticating, create a project for it and land in the chat
  // with that prompt ready to send. Otherwise go to the projects dashboard.
  const proceedAfterAuth = async () => {
    const trimmedMessage = initialMessage?.trim();
    if (trimmedMessage) {
      try {
        const project = await api.createProject(trimmedMessage.slice(0, 60) || DEFAULT_PROJECT_NAME);
        navigate(`/projects/${project.id}`, { state: { initialMessage: trimmedMessage } });
        return;
      } catch (error) {
        console.error("Failed to create project from initial prompt:", error);
      }
    }
    navigate("/projects");
  };

  const handleSignIn = async (e: React.FormEvent) => {
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
      await proceedAfterAuth();
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

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name || !email || !password) {
      toast({
        title: "Missing details",
        description: "Please fill in name, email, and password",
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
      setOpen(false);
      await proceedAfterAuth();
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

  const switchToMode = (nextMode: AuthMode) => {
    setMode(nextMode);
    clearForm();
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      {trigger ? (
        <DialogTrigger asChild>{trigger}</DialogTrigger>
      ) : !isControlled ? (
        <DialogTrigger asChild>
          <Button className="rounded-full px-8 py-3 text-base font-medium">Log in</Button>
        </DialogTrigger>
      ) : null}

      <DialogContent className="max-w-md p-5 sm:p-6">
        <DialogHeader className="text-center">
          <button
            type="button"
            onClick={() => {
              setOpen(false);
              navigate("/");
            }}
            className="mx-auto mb-2 flex items-center justify-center bg-transparent p-0 shadow-none transition hover:opacity-90 sm:mx-auto"
          >
            <Logo hideText className="scale-100" />
          </button>
          <DialogTitle className="text-xl font-semibold text-center">
            {mode === "signin" ? "Log in to your account" : "Sign up with email"}
          </DialogTitle>
        </DialogHeader>

        <div className="mt-2">
          {mode === "signin" ? (
            <form onSubmit={handleSignIn} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="home-auth-email" className="text-sm font-medium text-foreground">
                  Email
                </Label>
                <div className="relative">
                  <Mail className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="home-auth-email"
                    type="email"
                    placeholder="you@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="h-11 rounded-xl border-border/50 bg-muted/50 pl-10 text-sm focus:border-primary"
                    disabled={isLoading}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="home-auth-password" className="text-sm font-medium text-foreground">
                  Password
                </Label>
                <div className="relative">
                  <Lock className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="home-auth-password"
                    type="password"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="h-11 rounded-xl border-border/50 bg-muted/50 pl-10 text-sm focus:border-primary"
                    disabled={isLoading}
                  />
                </div>
              </div>

              <Button type="submit" disabled={isLoading} className="w-full rounded-xl px-6 py-2.5 text-base font-medium">
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Logging in...
                  </>
                ) : (
                  "Log in"
                )}
              </Button>

              <div className="pt-2 text-center text-sm text-muted-foreground">
                Don't have an account?{" "}
                <button
                  type="button"
                  onClick={() => switchToMode("signup")}
                  className="font-medium text-primary underline-offset-4 transition hover:underline"
                >
                  Sign up with email
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={handleSignUp} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="home-auth-name" className="text-sm font-medium text-foreground">
                  Full Name
                </Label>
                <div className="relative">
                  <User className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="home-auth-name"
                    type="text"
                    placeholder="John Doe"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="h-11 rounded-xl border-border/50 bg-muted/50 pl-10 text-sm focus:border-primary"
                    disabled={isLoading}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="home-auth-signup-email" className="text-sm font-medium text-foreground">
                  Email
                </Label>
                <div className="relative">
                  <Mail className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="home-auth-signup-email"
                    type="email"
                    placeholder="you@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="h-11 rounded-xl border-border/50 bg-muted/50 pl-10 text-sm focus:border-primary"
                    disabled={isLoading}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="home-auth-signup-password" className="text-sm font-medium text-foreground">
                  Password
                </Label>
                <div className="relative">
                  <Lock className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="home-auth-signup-password"
                    type="password"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="h-11 rounded-xl border-border/50 bg-muted/50 pl-10 text-sm focus:border-primary"
                    disabled={isLoading}
                  />
                </div>
              </div>

              <Button type="submit" disabled={isLoading} className="w-full rounded-xl px-6 py-2.5 text-base font-medium">
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Creating account...
                  </>
                ) : (
                  "Sign up"
                )}
              </Button>

              <div className="pt-2 text-center text-sm text-muted-foreground">
                Already have an account?{" "}
                <button
                  type="button"
                  onClick={() => switchToMode("signin")}
                  className="font-medium text-primary underline-offset-4 transition hover:underline"
                >
                  Log in
                </button>
              </div>
            </form>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}