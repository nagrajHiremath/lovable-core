import { useState } from "react";
import { ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { HomeAuthDialog } from "@/components/HomeAuthDialog";
import { Input } from "@/components/ui/input";
import { Logo } from "@/components/Logo";

const Index = () => {
  const [message, setMessage] = useState("");
  const [isPromptOpen, setIsPromptOpen] = useState(false);

  const handleSend = () => {
    if (!message.trim()) return;
    setIsPromptOpen(true);
  };

  return (
    <main className="relative min-h-screen overflow-hidden bg-[#03050d] text-foreground">
      {/* Dynamic Animated Gradient Layers */}
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(59,130,246,0.75),transparent_40%),radial-gradient(circle_at_80%_15%,rgba(168,85,247,0.7),transparent_38%),radial-gradient(circle_at_75%_75%,rgba(236,72,153,0.65),transparent_38%),radial-gradient(circle_at_25%_75%,rgba(14,165,233,0.6),transparent_40%)] animate-gradient-pan" />
      <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(120deg,rgba(59,130,246,0.28)_0%,rgba(168,85,247,0.38)_35%,rgba(236,72,153,0.32)_65%,rgba(14,165,233,0.26)_100%)] bg-[length:260%_260%] animate-galaxy-flow mix-blend-screen" />
      <div className="pointer-events-none absolute inset-[-25%] bg-[conic-gradient(from_180deg_at_50%_50%,rgba(59,130,246,0.3),rgba(168,85,247,0.22),rgba(236,72,153,0.3),rgba(14,165,233,0.22),rgba(59,130,246,0.3))] blur-[85px] animate-galaxy-spin mix-blend-screen" />
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_50%_42%,rgba(255,255,255,0.14),transparent_22%),radial-gradient(circle_at_center,transparent_35%,rgba(3,5,13,0.08)_65%,rgba(3,5,13,0.85)_100%)]" />
      <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,rgba(3,5,13,0.01)_0%,rgba(3,5,13,0.08)_38%,rgba(3,5,13,0.45)_100%)]" />

      {/* Floating Animated Color Blobs */}
      <div className="pointer-events-none absolute -left-20 top-4 h-[38rem] w-[38rem] rounded-full bg-cyan-400/50 blur-[95px] animate-blob-drift" />
      <div className="pointer-events-none absolute right-[-4rem] top-10 h-[44rem] w-[44rem] rounded-full bg-fuchsia-500/45 blur-[105px] animate-blob-drift [animation-delay:-2.5s]" />
      <div className="pointer-events-none absolute bottom-[-6rem] left-1/2 h-[40rem] w-[40rem] -translate-x-1/2 rounded-full bg-violet-500/40 blur-[95px] animate-blob-drift [animation-delay:-4.5s]" />
      <div className="pointer-events-none absolute left-[15%] top-[55%] h-64 w-64 rounded-full bg-pink-400/35 blur-[70px] animate-blob-drift [animation-delay:-1.5s]" />
      <div className="pointer-events-none absolute right-[18%] top-[30%] h-60 w-60 rounded-full bg-amber-400/30 blur-[65px] animate-blob-drift [animation-delay:-5.5s]" />

      <div className="relative z-10 mx-auto flex min-h-screen max-w-6xl flex-col px-6 py-6 lg:px-8">
        <nav className="flex items-center justify-between py-4">
          <div className="flex items-center gap-4">
            <Logo />
          </div>

          <div className="flex items-center gap-3">
            <HomeAuthDialog />
          </div>
        </nav>

        <section className="mx-auto mt-10 flex w-full max-w-5xl flex-col gap-8 text-center lg:mt-12">
          <div className="space-y-3">
            <p className="text-sm uppercase tracking-[0.45em] text-muted-foreground">Build something with AI</p>
            <h2 className="bg-gradient-to-r from-[#FF6A5C] via-[#E056A7] to-[#7C4DFF] bg-clip-text text-4xl font-semibold text-transparent tracking-[-0.04em] sm:text-6xl">
              Ask to build now
            </h2>
            <p className="mx-auto max-w-3xl text-sm leading-7 text-slate-300 sm:text-base sm:leading-8">
              Describe a feature, generate content, or explore a product workflow. Log in to start building with AI.
            </p>
          </div>

          <div className="mx-auto w-full max-w-4xl rounded-[1.5rem] border border-white/10 bg-[linear-gradient(135deg,rgba(255,255,255,0.08),rgba(255,255,255,0.03))] p-2.5 shadow-[0_30px_70px_rgba(15,23,42,0.38)] backdrop-blur-xl sm:p-3">
            <div className="rounded-[1.25rem] border border-white/10 bg-slate-950/80 p-2.5 shadow-[inset_0_1px_0_rgba(255,255,255,0.04)] sm:p-3">
              <div className="relative flex items-center gap-3 rounded-[1rem] border border-white/10 bg-[linear-gradient(135deg,rgba(59,130,246,0.14),rgba(168,85,247,0.1),rgba(236,72,153,0.14))] p-2 shadow-inner shadow-black/20">

                <Input
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  placeholder="Describe what you want to build..."
                  className="min-w-0 flex-1 border-0 bg-transparent px-4 text-sm text-white placeholder:text-slate-400 focus-visible:ring-0 focus-visible:ring-offset-0"
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && !e.shiftKey) {
                      e.preventDefault();
                      if (message.trim()) setIsPromptOpen(true);
                    }
                  }}
                />

                <Button
                  type="button"
                  onClick={handleSend}
                  className="h-10 w-10 rounded-[0.85rem] bg-white text-slate-950 shadow-[0_10px_30px_rgba(255,255,255,0.18)] transition hover:bg-cyan-100"
                  aria-label="Send prompt"
                >
                  <ArrowRight className="h-4 w-4" />
                </Button>
              </div>

            </div>
          </div>
        </section>
      </div>

      <HomeAuthDialog open={isPromptOpen} onOpenChange={setIsPromptOpen} initialMessage={message} />
    </main>
  );
};

export default Index;
