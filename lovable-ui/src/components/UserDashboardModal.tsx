import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { api, getUserInfo } from "@/lib/api";
import { UserProfileResponse, SubscriptionResponse, PlanResponse } from "@/lib/types";
import { useToast } from "@/hooks/use-toast";
import { 
  User, 
  CreditCard, 
  Zap, 
  CheckCircle2, 
  ExternalLink, 
  Loader2, 
  Sparkles, 
  ShieldCheck, 
  Clock, 
  FolderCheck,
  Building2
} from "lucide-react";

interface UserDashboardModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function UserDashboardModal({ open, onOpenChange }: UserDashboardModalProps) {
  const { toast } = useToast();
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [subscription, setSubscription] = useState<SubscriptionResponse | null>(null);
  const [plans, setPlans] = useState<PlanResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      loadData();
    }
  }, [open]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [profData, subData, planData] = await Promise.all([
        api.getCurrentProfile(),
        api.getMySubscription(),
        api.getPlans(),
      ]);
      setProfile(profData);
      setSubscription(subData);
      setPlans(planData);
    } catch (error) {
      console.error("Failed to load account dashboard data:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenPortal = async () => {
    setActionLoading("portal");
    try {
      const { portalUrl } = await api.openCustomerPortal();
      if (portalUrl && portalUrl !== "https://stripe.com") {
        window.location.href = portalUrl;
      } else {
        toast({
          title: "Billing Portal Opened",
          description: "Stripe Customer Portal endpoint triggered successfully.",
        });
      }
    } catch (error) {
      toast({
        title: "Portal Error",
        description: "Failed to open billing portal",
        variant: "destructive",
      });
    } finally {
      setActionLoading(null);
    }
  };

  const handleCheckout = async (planId: number) => {
    setActionLoading(`checkout-${planId}`);
    try {
      const { url } = await api.createCheckoutSession(planId);
      if (url && url !== "https://stripe.com") {
        window.location.href = url;
      } else {
        toast({
          title: "Checkout Session Created",
          description: `Checkout session for Plan #${planId} initialized.`,
        });
      }
    } catch (error) {
      toast({
        title: "Checkout Error",
        description: "Failed to create checkout session",
        variant: "destructive",
      });
    } finally {
      setActionLoading(null);
    }
  };

  const localUser = getUserInfo();
  const name = profile?.name || localUser?.name || localUser?.username?.split("@")[0] || "Authenticated User";
  const username = profile?.username || localUser?.username || "";

  const currentPlan = subscription?.plan;
  const tokensUsed = subscription?.tokensUsedThisCycle || 0;
  const maxTokens = currentPlan?.maxTokensPerDay || 0;
  const tokenPercentage = maxTokens > 0 ? Math.min(100, Math.round((tokensUsed / maxTokens) * 100)) : 0;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl border-white/10 bg-slate-950/95 text-foreground backdrop-blur-2xl p-6 sm:p-8 max-h-[90vh] overflow-y-auto">
        <DialogHeader className="border-b border-white/10 pb-4">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-[#FF6A5C] via-[#E056A7] to-[#7C4DFF] text-white shadow-lg">
              <User className="h-6 w-6" />
            </div>
            <div>
              <DialogTitle className="text-2xl font-bold tracking-tight text-white">
                Account & Subscription Dashboard
              </DialogTitle>
              <DialogDescription className="text-slate-400 text-sm">
                Manage your user profile, AI token limits, and subscription billing plans.
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-16 gap-3">
            <Loader2 className="h-10 w-10 animate-spin text-pink-500" />
            <p className="text-sm text-slate-400">Loading your profile & subscription info...</p>
          </div>
        ) : (
          <div className="space-y-6 pt-4">
            {/* User Profile Banner */}
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 rounded-2xl border border-white/10 bg-white/5 p-5 backdrop-blur-md">
              <div className="flex items-center gap-4">
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-pink-500 to-violet-600 text-white font-bold text-xl shadow-md">
                  {name.charAt(0).toUpperCase()}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-lg font-bold text-white">{name}</h3>
                    <Badge variant="outline" className="border-emerald-500/30 bg-emerald-500/10 text-emerald-400 text-[10px] font-bold">
                      <ShieldCheck className="h-3 w-3 mr-1" /> Active Account
                    </Badge>
                  </div>
                  {username && <p className="text-sm text-slate-400">{username}</p>}
                </div>
              </div>

              <Button
                variant="outline"
                onClick={handleOpenPortal}
                disabled={actionLoading === "portal"}
                className="rounded-xl border-white/10 bg-white/5 hover:bg-white/10 text-sm font-medium gap-2"
              >
                {actionLoading === "portal" ? <Loader2 className="h-4 w-4 animate-spin" /> : <ExternalLink className="h-4 w-4 text-pink-400" />}
                Customer Portal
              </Button>
            </div>

            {/* Current Active Subscription Status */}
            <div className="rounded-2xl border border-white/10 bg-slate-900/60 p-6 backdrop-blur-md space-y-5">
              <div className="flex flex-wrap items-center justify-between gap-3 border-b border-white/10 pb-4">
                <div className="flex items-center gap-2.5">
                  <Zap className="h-5 w-5 text-amber-400" />
                  <h4 className="text-base font-bold text-white">Subscription Status</h4>
                </div>
                <div className="flex items-center gap-2">
                  <Badge className="bg-gradient-to-r from-pink-500 to-purple-600 text-white px-3 py-1 text-xs font-bold shadow-md">
                    {currentPlan?.name || "Free Plan"}
                  </Badge>
                  <Badge variant="outline" className="border-emerald-500/30 bg-emerald-500/20 text-emerald-300 text-xs font-semibold">
                    {subscription?.status || "FREE"}
                  </Badge>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-sm">
                <div className="rounded-xl border border-white/5 bg-white/[0.02] p-4">
                  <div className="text-xs text-slate-400 flex items-center gap-1.5 mb-1">
                    <FolderCheck className="h-4 w-4 text-pink-400" />
                    <span>Project Limit</span>
                  </div>
                  <p className="text-lg font-bold text-white">
                    {currentPlan?.maxProjects ? `Up to ${currentPlan.maxProjects} Projects` : "Standard Limit"}
                  </p>
                </div>

                <div className="rounded-xl border border-white/5 bg-white/[0.02] p-4">
                  <div className="text-xs text-slate-400 flex items-center gap-1.5 mb-1">
                    <Sparkles className="h-4 w-4 text-purple-400" />
                    <span>Unlimited AI</span>
                  </div>
                  <p className="text-lg font-bold text-white">
                    {currentPlan?.unlimitedAi ? "Enabled" : "Standard"}
                  </p>
                </div>

                <div className="rounded-xl border border-white/5 bg-white/[0.02] p-4">
                  <div className="text-xs text-slate-400 flex items-center gap-1.5 mb-1">
                    <Clock className="h-4 w-4 text-sky-400" />
                    <span>Billing Renewal</span>
                  </div>
                  <p className="text-sm font-semibold text-white truncate">
                    {subscription?.currentPeriodEnd 
                      ? new Date(subscription.currentPeriodEnd).toLocaleDateString()
                      : "No active renewal"}
                  </p>
                </div>
              </div>

              {/* Token Usage Progress */}
              {maxTokens > 0 && (
                <div className="space-y-2 pt-2">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-400 flex items-center gap-1">
                      <Zap className="h-3.5 w-3.5 text-pink-400" /> Daily AI Token Consumption
                    </span>
                    <span className="font-semibold text-white">
                      {tokensUsed.toLocaleString()} / {maxTokens.toLocaleString()} Tokens ({tokenPercentage}%)
                    </span>
                  </div>
                  <Progress value={tokenPercentage} className="h-2.5 bg-white/10" />
                </div>
              )}
            </div>

            {/* Plans Comparison Grid */}
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h4 className="text-lg font-bold text-white flex items-center gap-2">
                  <Building2 className="h-5 w-5 text-pink-400" />
                  Available Subscription Plans
                </h4>
              </div>

              {plans.length === 0 ? (
                <div className="rounded-xl border border-white/10 bg-white/5 p-6 text-center text-slate-400 text-sm">
                  No subscription plans currently published by backend API (`GET /api/plan`).
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {plans.map((plan) => {
                    const isCurrent = currentPlan?.id === plan.id;
                    return (
                      <div
                        key={plan.id}
                        className={`relative flex flex-col justify-between rounded-2xl border p-5 transition-all duration-300 ${
                          isCurrent
                            ? "border-pink-500/50 bg-pink-500/10 shadow-[0_0_20px_rgba(236,72,153,0.15)]"
                            : "border-white/10 bg-slate-900/40 hover:border-white/20 hover:bg-slate-900/70"
                        }`}
                      >
                        <div>
                          {isCurrent && (
                            <span className="absolute -top-3 right-4 rounded-full bg-pink-500 px-3 py-0.5 text-[10px] font-bold uppercase tracking-wider text-white shadow-md">
                              Current Plan
                            </span>
                          )}
                          <h5 className="text-lg font-bold text-white">{plan.name}</h5>
                          <p className="text-2xl font-extrabold text-pink-400 my-2">{plan.price}</p>

                          <ul className="space-y-2 text-xs text-slate-300 mt-4">
                            <li className="flex items-center gap-2">
                              <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                              <span>Up to <strong>{plan.maxProjects}</strong> Projects</span>
                            </li>
                            <li className="flex items-center gap-2">
                              <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                              <span><strong>{plan.maxTokensPerDay.toLocaleString()}</strong> Tokens / day</span>
                            </li>
                            <li className="flex items-center gap-2">
                              <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                              <span>{plan.unlimitedAi ? "Unlimited AI Generation" : "Standard AI Generation"}</span>
                            </li>
                          </ul>
                        </div>

                        <div className="mt-6 pt-4 border-t border-white/10">
                          {isCurrent ? (
                            <Button disabled className="w-full rounded-xl bg-white/10 text-slate-300 cursor-default">
                              Active Plan
                            </Button>
                          ) : (
                            <Button
                              onClick={() => handleCheckout(plan.id)}
                              disabled={actionLoading === `checkout-${plan.id}`}
                              className="w-full rounded-xl bg-gradient-to-r from-[#FF6A5C] via-[#E056A7] to-[#7C4DFF] text-white font-semibold hover:opacity-90 transition"
                            >
                              {actionLoading === `checkout-${plan.id}` ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                              ) : (
                                `Select ${plan.name}`
                              )}
                            </Button>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
